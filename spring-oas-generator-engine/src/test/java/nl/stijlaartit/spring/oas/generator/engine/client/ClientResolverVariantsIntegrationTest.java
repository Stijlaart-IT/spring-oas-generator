package nl.stijlaartit.spring.oas.generator.engine.client;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiFile;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiOperation;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaMethodName;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.OasSimplifier;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.model.TypeInfoResolver;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientResolverVariantsIntegrationTest {

    @Test
    void resolvesRefResponseBody_hasGetRefResponseBodyOperationWithRefResponseType() {
        List<ApiFile> clients = resolveClient(parseVariantsSpec());

        ApiOperation operation = clients.stream()
                .flatMap(client -> client.getOperations().stream())
                .filter(op -> op.name().equals(new JavaMethodName("getRefResponseBody")))
                .findFirst()
                .orElseThrow();

        assertEquals(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("RefResponse")), operation.responseType());
    }

    private List<ApiFile> resolveClient(OpenAPI openAPI) {
        OasSimplifier oasSimplifier = new OasSimplifier(Logger.noOp());
        SimplifiedOas simplifiedOas = oasSimplifier.simplify(openAPI);
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeInfoResolver typeInfoResolver = TypeInfoResolver.resolve(schemaTypes, "com.example.models");
        return new ClientResolver(Logger.noOp(), typeInfoResolver).resolve(simplifiedOas);
    }

    private OpenAPI parseVariantsSpec() {
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation("../examples/variants.yml", null, null);
        return Objects.requireNonNull(result.getOpenAPI(), "Could not parse ../examples/variants.yml");
    }
}
