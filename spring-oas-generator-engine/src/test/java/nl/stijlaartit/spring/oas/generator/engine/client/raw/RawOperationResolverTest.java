package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RawOperationResolverTest {

    @Test
    void resolvesMultiple2xxResponsesWithEquivalentInlineSchemas() {
        Schema<?> schema200 = new Schema<>().type("string").format("uuid");
        Schema<?> schema201 = new Schema<>().type("string").format("uuid");

        assertThat(schema200).isNotSameAs(schema201);
        assertThat(schema200).isEqualTo(schema201);

        ApiResponses responses = new ApiResponses()
                .addApiResponse("200", responseWithSchema(schema200))
                .addApiResponse("201", responseWithSchema(schema201));

        OpenAPI openAPI = openApiWithResponses("getThings", responses);
        RawOperationResolver resolver = new RawOperationResolver(Logger.noOp(), openAPI);

        List<RawOperation> operations = resolver.resolve();
        assertThat(operations).hasSize(1);
        assertThat(operations.get(0)).isInstanceOf(GeneratableOperation.class);

        GeneratableOperation operation = (GeneratableOperation) operations.get(0);
        assertThat(operation.responseBodyType()).isInstanceOf(ResponseBodyType.SchemaType.class);

        ResponseBodyType.SchemaType responseBodyType = (ResponseBodyType.SchemaType) operation.responseBodyType();
        assertThat(responseBodyType.schema()).isSameAs(schema200);
    }

    @Test
    void failsWhenMultiple2xxResponsesDefineDifferentSchemas() {
        Schema<?> schema200 = new Schema<>().type("string");
        Schema<?> schema201 = new Schema<>().type("integer");

        ApiResponses responses = new ApiResponses()
                .addApiResponse("200", responseWithSchema(schema200))
                .addApiResponse("201", responseWithSchema(schema201));

        OpenAPI openAPI = openApiWithResponses("getThings", responses);
        RawOperationResolver resolver = new RawOperationResolver(Logger.noOp(), openAPI);

        assertThatThrownBy(resolver::resolve)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Multiple 2xx responses with body defined for operation");
    }

    private static OpenAPI openApiWithResponses(String operationId, ApiResponses responses) {
        Operation operation = new Operation()
                .operationId(operationId)
                .responses(responses);

        PathItem pathItem = new PathItem().get(operation);
        Paths paths = new Paths().addPathItem("/things", pathItem);
        return new OpenAPI().paths(paths);
    }

    private static ApiResponse responseWithSchema(Schema<?> schema) {
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("application/json", mediaType);
        return new ApiResponse().content(content);
    }
}
