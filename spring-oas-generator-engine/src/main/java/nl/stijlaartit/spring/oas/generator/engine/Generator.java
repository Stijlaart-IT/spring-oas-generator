package nl.stijlaartit.spring.oas.generator.engine;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;
import nl.stijlaartit.spring.oas.generator.engine.client.ClientResolver;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.model.ModelResolver;
import nl.stijlaartit.spring.oas.generator.engine.model.TypeInfoResolver;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.utility.UtilityResolver;
import nl.stijlaartit.spring.oas.generator.serialization.ClientSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.EnumModelSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.GenerationFileSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.ImplementsByMapping;
import nl.stijlaartit.spring.oas.generator.serialization.JacksonV2NullWrapperSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.JacksonV3NullWrapperSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.PackageInfoSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.RecordModelSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.SerializedFile;
import nl.stijlaartit.spring.oas.generator.serialization.SpringConfigSerializer;
import nl.stijlaartit.spring.oas.generator.serialization.UnionModelSerializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Generator {

    private final Logger logger;

    public Generator(Logger logger) {
        this.logger = logger;
    }

    public void generate(GeneratorConfig config) throws Exception {
        String modelsPackage = config.outputPackage() + ".models";
        String clientPackage = config.outputPackage() + ".client";
        String configPackage = config.outputPackage() + ".config";

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(config.specFile().toString(), null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            StringBuilder error = new StringBuilder("Failed to parse OpenAPI spec: " + config.specFile());
            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                for (String message : result.getMessages()) {
                    error.append(System.lineSeparator()).append(message);
                }
            }
            throw new IllegalArgumentException(error.toString());
        }

        OasSimplifier oasSimplifier = new OasSimplifier(logger);
        final SimplifiedOas simplifiedOas = oasSimplifier.simplify(openAPI);

        final var registry = SchemaRegistry.resolve(simplifiedOas);
        final var nameProvider = NameProvider.create();

        logger.info("Found [" + registry.getInstances().size() + "] schema(s)");
        final var schemaTypeResolver = new SchemaTypeResolver(registry, nameProvider, logger);
        final var schemaTypes = schemaTypeResolver.resolve();
        logger.info("Found [" + schemaTypes.types().size() + "] distinct schema(s)");
        final var typeInfoResolver = TypeInfoResolver.resolve(schemaTypes, modelsPackage);

        final var modelResolver = new ModelResolver(schemaTypes, typeInfoResolver, logger);
        final var clientResolver = new ClientResolver(logger, typeInfoResolver);
        final var utilityResolver = new UtilityResolver(modelsPackage, clientPackage, configPackage, config.springConfigGenerationConfig());

        final var modelFiles = modelResolver.resolve();
        logger.info("Found [" + modelFiles.size() + "] model(s) to generate");
        final var clientFiles = clientResolver.resolve(simplifiedOas);
        logger.info("Found [" + clientFiles.size() + "] client(s) to generate");
        final var utilityFiles = utilityResolver.resolve(modelFiles, clientFiles);
        logger.info("Found [" + utilityFiles.size() + "] utility(s) to generate");

        final var generationFiles = new ArrayList<GenerationFile>();
        generationFiles.addAll(modelFiles);
        generationFiles.addAll(clientFiles);
        generationFiles.addAll(utilityFiles);

        ImplementsByMapping implementsByModel = ImplementsByMapping.create(generationFiles);
        GenerationFileSerializer<? extends GenerationFile> nullWrapperSerializer =
                switch (config.nullWrapperSerializerConfig().jacksonVersion()) {
                    case V2 -> new JacksonV2NullWrapperSerializer(modelsPackage);
                    case V3 -> new JacksonV3NullWrapperSerializer(modelsPackage);
                };
        final var serializers = List.of(
                new RecordModelSerializer(modelsPackage, config.recordModelWriterConfig(), implementsByModel),
                new EnumModelSerializer(modelsPackage, implementsByModel),
                new UnionModelSerializer(modelsPackage),
                new ClientSerializer(clientPackage, modelsPackage, config.clientWriterConfig()),
                new SpringConfigSerializer(),
                new PackageInfoSerializer(),
                nullWrapperSerializer
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
            Path outputPath = serializedFile.writeTo(config.outputDirectory());
            logger.debug("Wrote to [" + outputPath + "]");
        }

    }
}
