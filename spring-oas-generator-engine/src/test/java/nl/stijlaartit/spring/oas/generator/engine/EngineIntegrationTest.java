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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Objects;

public class EngineIntegrationTest {

    @ParameterizedTest
    @MethodSource("nl.stijlaartit.spring.oas.generator.engine.EngineIntegrationTest#specs")
    void test(String specName) {
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation("../examples/" + specName, null, null);
        OpenAPI openAPI = Objects.requireNonNull(result.getOpenAPI());
        Logger logger = Logger.noOp();
        String modelsPackage = "com.example.models";
        String apiPackage = "com.example.api";

        OasSimplifier oasSimplifier = new OasSimplifier(logger);
        final SimplifiedOas simplifiedOas = oasSimplifier.simplify(openAPI);

        final var registry = SchemaRegistry.resolve(simplifiedOas);
        final var nameProvider = NameProvider.create();

        final var schemaTypeResolver = new SchemaTypeResolver(registry, nameProvider, logger);
        final var schemaTypes = schemaTypeResolver.resolve();

        final var typeInfoResolver = TypeInfoResolver.resolve(schemaTypes, modelsPackage);

        final var modelResolver = new ModelResolver(schemaTypes, typeInfoResolver, Logger.noOp());
        final var clientResolver = new ClientResolver(logger, typeInfoResolver);

        final var utilityResolver = new UtilityResolver(modelsPackage, apiPackage);

        final var modelFiles = modelResolver.resolve();
        final var clientFiles = clientResolver.resolve(simplifiedOas);
        final var utilityFiles = utilityResolver.resolve(modelFiles, clientFiles);

        final var generationFiles = new ArrayList<GenerationFile>();
        generationFiles.addAll(modelFiles);
        generationFiles.addAll(clientFiles);
        generationFiles.addAll(utilityFiles);
    }


    public static String[] specs() {
        return new String[]{
                "petstore.json",
                "pokeapi.yml",
                "realworld.yml",
                "spotify.yml",
                "variants.yml"
        };
    }
}
