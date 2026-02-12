package nl.stijlaartit.generator.engine;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.generator.engine.client.ClientResolver;
import nl.stijlaartit.generator.engine.client.ClientWriter;
import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.GenerationFileWriter;
import nl.stijlaartit.generator.engine.domain.WriteReport;
import nl.stijlaartit.generator.engine.model.ModelResolver;
import nl.stijlaartit.generator.engine.model.ModelWriter;
import nl.stijlaartit.generator.engine.model.RecordModelWriterConfig;
import nl.stijlaartit.generator.engine.model.TypeDescriptorFactory;
import nl.stijlaartit.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.generator.engine.schematype.SchemaTypeResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

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

        final var registry = SchemaRegistry.resolve(openAPI);
        final var schemaTypeResolver = new SchemaTypeResolver();
        final var schemaTypes = schemaTypeResolver.resolve(registry);
        final var typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes, registry);

        final var modelResolver = new ModelResolver(registry);
        final var modelWriter = new ModelWriter(modelsPackage, RecordModelWriterConfig.defaultConfig());
        final var clientResolver = new ClientResolver(typeDescriptorFactory);
        final var clientWriter = new ClientWriter(clientPackage, modelsPackage);

        final var modelFiles = modelResolver.resolve(schemaTypes);
        final var clientFiles = clientResolver.resolve(openAPI);
        writeAspect("model", modelWriter, modelFiles, outputDirectory);
        writeAspect("client", clientWriter, clientFiles, outputDirectory);
    }

    private static <T extends GenerationFile> void writeAspect(String aspectId, GenerationFileWriter<T> writer, List<T> files, Path output)
            throws java.io.IOException {
        if (files.isEmpty()) {
            return;
        }

        WriteReport report = writer.writeAll(files, output);
        for (Path filePath : report.getFiles()) {
            if (!Files.exists(filePath)) {
                throw new java.io.IOException("Expected generated file to exist: " + filePath);
            }
        }
        String prefix = "[" + aspectId + "] ";
        System.out.println(prefix + "Wrote " + report.getTotalFiles() + " file(s).");
        report.getCountsByDirectory().forEach((directory, count) ->
                System.out.println(prefix + "Wrote " + count + " file(s) to " + directory));
    }
}
