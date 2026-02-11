package nl.stijlaartit.generator.engine;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.generator.engine.client.ClientGenerationAspect;
import nl.stijlaartit.generator.engine.model.ModelGenerationAspect;
import nl.stijlaartit.generator.engine.domain.GenerationAspect;
import nl.stijlaartit.generator.engine.domain.GenerationContext;
import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.GenerationFileWriter;
import nl.stijlaartit.generator.engine.domain.WriteReport;

import java.nio.file.Files;
import java.nio.file.Path;
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

public class Generator {

    public void generate(Path specFile, Path outputDirectory, String outputPackage) throws Exception {
        Objects.requireNonNull(specFile, "specFile");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(outputPackage, "outputPackage");

        String modelsPackage = outputPackage + ".models";
        String clientPackage = outputPackage + ".client";

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(specFile.toString(), null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            StringBuilder error = new StringBuilder("Failed to parse OpenAPI spec: " + specFile);
            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                for (String message : result.getMessages()) {
                    error.append(System.lineSeparator()).append(message);
                }
            }
            throw new IllegalArgumentException(error.toString());
        }

        List<GenerationAspect> aspects = List.of(
                new ModelGenerationAspect(modelsPackage),
                new ClientGenerationAspect(clientPackage, modelsPackage)
        );

        GenerationContext context = new GenerationContext();
        List<GenerationAspect> orderedAspects = orderAspects(aspects);
        for (GenerationAspect aspect : orderedAspects) {
            @SuppressWarnings("unchecked")
            var resolver = (nl.stijlaartit.generator.engine.domain.Resolver<OpenAPI>) aspect.getResolver();
            resolver.resolve(openAPI, context);
        }
        ensureAllFilesHaveWriters(orderedAspects, context);
        for (GenerationAspect aspect : orderedAspects) {
            writeAspect(aspect, context, outputDirectory);
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
