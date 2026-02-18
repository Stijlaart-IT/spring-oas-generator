package nl.stijlaartit.generator.engine;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.generator.engine.client.ClientResolver;
import nl.stijlaartit.generator.engine.client.ClientWriter;
import nl.stijlaartit.generator.engine.client.ClientWriterConfig;
import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.GenerationFileSerializer;
import nl.stijlaartit.generator.engine.domain.SerializedFile;
import nl.stijlaartit.generator.engine.model.EnumModelWriter;
import nl.stijlaartit.generator.engine.model.ImplementsByMapping;
import nl.stijlaartit.generator.engine.model.ModelResolver;
import nl.stijlaartit.generator.engine.model.NullWrapperWriter;
import nl.stijlaartit.generator.engine.model.RecordModelWriter;
import nl.stijlaartit.generator.engine.model.RecordModelWriterConfig;
import nl.stijlaartit.generator.engine.model.TypeDescriptorFactory;
import nl.stijlaartit.generator.engine.model.UnionModelWriter;
import nl.stijlaartit.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.generator.engine.utility.PackageInfoWriter;
import nl.stijlaartit.generator.engine.utility.UtilityResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        final var clientResolver = new ClientResolver(typeDescriptorFactory);
        final var utilityResolver = new UtilityResolver(modelsPackage, clientPackage);

        final var modelFiles = modelResolver.resolve(schemaTypes);
        final var clientFiles = clientResolver.resolve(openAPI);
        final var utilityFiles = utilityResolver.resolve(modelFiles, clientFiles);

        final var generationFiles = new ArrayList<GenerationFile>();
        generationFiles.addAll(modelFiles);
        generationFiles.addAll(clientFiles);
        generationFiles.addAll(utilityFiles);

        ImplementsByMapping implementsByModel = ImplementsByMapping.create(generationFiles);
        final var serializers = List.of(
                new RecordModelWriter(modelsPackage, RecordModelWriterConfig.defaultConfig(), implementsByModel),
                new EnumModelWriter(modelsPackage, implementsByModel),
                new UnionModelWriter(modelsPackage),
                new ClientWriter(clientPackage, modelsPackage, ClientWriterConfig.defaultConfig()),
                new PackageInfoWriter(),
                new NullWrapperWriter(modelsPackage)
        );

        final var serializedFiles = new ArrayList<SerializedFile>();
        for (GenerationFile generationFile : generationFiles) {
            Optional<GenerationFileSerializer<? extends GenerationFile>> first = serializers.stream().filter(v -> v.supports(generationFile)).findFirst();
            if (first.isEmpty()) {
                throw new IllegalArgumentException("No serializer found for " + generationFile.getClass().getSimpleName());
            }

            GenerationFileSerializer<GenerationFile> generationFileSerializer = (GenerationFileSerializer<GenerationFile>) first.orElseThrow();
            serializedFiles.add(generationFileSerializer.serialize(generationFile));
        }

        writeSerializedFiles(outputDirectory, serializedFiles);
    }

    private static void writeSerializedFiles(Path outputDirectory, ArrayList<SerializedFile> serializedFiles) throws IOException {
        for (SerializedFile serializedFile : serializedFiles) {
            serializedFile.writeTo(outputDirectory);
        }
    }
}
