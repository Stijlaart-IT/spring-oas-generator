package nl.stijlaartit.restclient;

import nl.stijlaartit.generation.client.ClientGenerationAspect;
import nl.stijlaartit.generation.model.ModelGenerationAspect;
import nl.stijlaartit.generator.domain.GenerationAspect;
import nl.stijlaartit.generator.domain.GenerationContext;
import nl.stijlaartit.generator.domain.GenerationFile;
import nl.stijlaartit.generator.domain.GenerationFileWriter;
import nl.stijlaartit.generator.domain.WriteReport;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

@SpringBootApplication
public class RestClientGeneratorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(RestClientGeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: <openapi-spec-file> <output-path> <output-package>");
            System.exit(1);
        }

        String specFile = args[0];
        String outputPath = args[1];
        String outputPackage = args[2];
        String modelsPackage = outputPackage + ".models";
        String clientPackage = outputPackage + ".client";

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specFile, null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            System.err.println("Failed to parse OpenAPI spec: " + specFile);
            if (result.getMessages() != null) {
                result.getMessages().forEach(System.err::println);
            }
            System.exit(1);
        }

        Path output = Path.of(outputPath);

        List<GenerationAspect> aspects = List.of(
                new ModelGenerationAspect(modelsPackage),
                new ClientGenerationAspect(clientPackage, modelsPackage)
        );
        GenerationContext context = new GenerationContext();
        List<GenerationAspect> orderedAspects = orderAspects(aspects);
        for (GenerationAspect aspect : orderedAspects) {
            @SuppressWarnings("unchecked")
            var resolver = (nl.stijlaartit.generator.domain.Resolver<OpenAPI>) aspect.getResolver();
            resolver.resolve(openAPI, context);
        }
        ensureAllFilesHaveWriters(orderedAspects, context);
        for (GenerationAspect aspect : orderedAspects) {
            writeAspect(aspect, context, output);
        }
    }

    private static List<GenerationAspect> orderAspects(List<GenerationAspect> aspects) {
        Map<String, GenerationAspect> byId = new LinkedHashMap<>();
        for (GenerationAspect aspect : aspects) {
            String id = Objects.requireNonNull(aspect.getId());
            if (byId.put(id, aspect) != null) {
                throw new IllegalArgumentException("Duplicate generation aspect id: " + id);
            }
        }

        Map<String, Set<String>> remainingDependencies = new LinkedHashMap<>();
        Map<String, Set<String>> dependents = new LinkedHashMap<>();
        for (GenerationAspect aspect : aspects) {
            Set<String> deps = new LinkedHashSet<>(aspect.getDependencies());
            for (String dep : deps) {
                if (!byId.containsKey(dep)) {
                    throw new IllegalArgumentException("Unknown dependency '" + dep
                            + "' for aspect '" + aspect.getId() + "'");
                }
                dependents.computeIfAbsent(dep, key -> new LinkedHashSet<>())
                        .add(aspect.getId());
            }
            remainingDependencies.put(aspect.getId(), deps);
        }

        Queue<String> ready = new ArrayDeque<>();
        for (GenerationAspect aspect : aspects) {
            if (remainingDependencies.get(aspect.getId()).isEmpty()) {
                ready.add(aspect.getId());
            }
        }

        List<GenerationAspect> ordered = new ArrayList<>();
        while (!ready.isEmpty()) {
            String id = ready.remove();
            ordered.add(byId.get(id));
            for (String dependentId : dependents.getOrDefault(id, Set.of())) {
                Set<String> deps = remainingDependencies.get(dependentId);
                deps.remove(id);
                if (deps.isEmpty()) {
                    ready.add(dependentId);
                }
            }
        }

        if (ordered.size() != aspects.size()) {
            throw new IllegalStateException("Cyclic generation aspect dependencies detected");
        }

        return ordered;
    }

    private static void writeAspect(GenerationAspect aspect, GenerationContext context, Path output)
            throws java.io.IOException {
        Optional<? extends GenerationFileWriter<? extends GenerationFile>> writerOptional = aspect.getWriter();
        if (writerOptional.isEmpty()) {
            return;
        }
        List<? extends GenerationFile> files = context.getFiles(aspect.getFileType());
        if (files.isEmpty()) {
            return;
        }
        @SuppressWarnings("unchecked")
        GenerationFileWriter<GenerationFile> writer = (GenerationFileWriter<GenerationFile>) writerOptional.get();
        WriteReport report = writer.writeAll((List<GenerationFile>) files, output);
        for (Path filePath : report.getFiles()) {
            if (!Files.exists(filePath)) {
                throw new java.io.IOException("Expected generated file to exist: " + filePath);
            }
        }
        String prefix = "[" + aspect.getId() + "] ";
        System.out.println(prefix + "Wrote " + report.getTotalFiles() + " file(s).");
        report.getCountsByDirectory().forEach((directory, count) ->
                System.out.println(prefix + "Wrote " + count + " file(s) to " + directory));
    }

    private static void ensureAllFilesHaveWriters(List<GenerationAspect> aspects, GenerationContext context) {
        List<GenerationAspect> writerAspects = aspects.stream()
                .filter(aspect -> aspect.getWriter().isPresent())
                .toList();
        for (GenerationFile file : context.getFiles()) {
            boolean supported = writerAspects.stream()
                    .anyMatch(aspect -> aspect.getFileType().isInstance(file));
            if (!supported) {
                throw new IllegalStateException("No writer available for generated file '"
                        + file.getName() + "' (" + file.getClass().getSimpleName() + ")");
            }
        }
    }
}
