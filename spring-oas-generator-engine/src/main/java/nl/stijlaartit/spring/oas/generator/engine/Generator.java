package nl.stijlaartit.spring.oas.generator.engine;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.spring.oas.generator.engine.client.ClientResolver;
import nl.stijlaartit.spring.oas.generator.engine.client.ClientSerializer;
import nl.stijlaartit.spring.oas.generator.engine.client.ClientWriterConfig;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFileSerializer;
import nl.stijlaartit.spring.oas.generator.engine.domain.SerializedFile;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.model.EnumModelSerializer;
import nl.stijlaartit.spring.oas.generator.engine.model.ImplementsByMapping;
import nl.stijlaartit.spring.oas.generator.engine.model.ModelResolver;
import nl.stijlaartit.spring.oas.generator.engine.model.NullWrapperSerializer;
import nl.stijlaartit.spring.oas.generator.engine.model.RecordModelSerializer;
import nl.stijlaartit.spring.oas.generator.engine.model.RecordModelWriterConfig;
import nl.stijlaartit.spring.oas.generator.engine.model.TypeDescriptorFactory;
import nl.stijlaartit.spring.oas.generator.engine.model.UnionModelSerializer;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.utility.PackageInfoSerializer;
import nl.stijlaartit.spring.oas.generator.engine.utility.UtilityResolver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Generator {

    private final Logger logger;

    public Generator(Logger logger) {
        this.logger = logger;
    }

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
        logger.info("Found [" + registry.getInstances().size() + "] schema(s)");
        final var schemaTypeResolver = new SchemaTypeResolver(registry);
        final var schemaTypes = schemaTypeResolver.resolve();
        logger.info("Found [" + schemaTypes.types().size() + "] distinct schema(s)");
        final var typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes, registry);

        final var modelResolver = new ModelResolver(registry);
        final var clientResolver = new ClientResolver(logger, typeDescriptorFactory);
        final var utilityResolver = new UtilityResolver(modelsPackage, clientPackage);

        final var modelFiles = modelResolver.resolve(schemaTypes);
        logger.info("Found [" + modelFiles.size() + "] model(s) to generate");
        final var clientFiles = clientResolver.resolve(openAPI);
        logger.info("Found [" + clientFiles.size() + "] client(s) to generate");
        final var utilityFiles = utilityResolver.resolve(modelFiles, clientFiles);
        logger.info("Found [" + utilityFiles.size() + "] utility(s) to generate");

        final var generationFiles = new ArrayList<GenerationFile>();
        generationFiles.addAll(modelFiles);
        generationFiles.addAll(clientFiles);
        generationFiles.addAll(utilityFiles);

        ImplementsByMapping implementsByModel = ImplementsByMapping.create(generationFiles);
        final var serializers = List.of(
                new RecordModelSerializer(modelsPackage, RecordModelWriterConfig.defaultConfig(), implementsByModel),
                new EnumModelSerializer(modelsPackage, implementsByModel),
                new UnionModelSerializer(modelsPackage),
                new ClientSerializer(clientPackage, modelsPackage, ClientWriterConfig.defaultConfig()),
                new PackageInfoSerializer(),
                new NullWrapperSerializer(modelsPackage)
        );

        final var serializedFiles = new ArrayList<SerializedFile>();
        for (GenerationFile generationFile : generationFiles) {
            Optional<GenerationFileSerializer<? extends GenerationFile>> first = serializers.stream().filter(v -> v.supports(generationFile)).findFirst();
            if (first.isEmpty()) {
                throw new IllegalArgumentException("No serializer found for " + generationFile.getClass().getSimpleName());
            }

            @SuppressWarnings("unchecked")
            GenerationFileSerializer<GenerationFile> generationFileSerializer = (GenerationFileSerializer<GenerationFile>) first.orElseThrow();

            serializedFiles.add(generationFileSerializer.serialize(generationFile));
        }

        logger.info("Serialized [" + serializedFiles.size() + "] file(s)");

        for (SerializedFile serializedFile : serializedFiles) {
            Path outputPath = serializedFile.writeTo(outputDirectory);
            logger.debug("Wrote to [" + outputPath + "]");
        }

    }
}
