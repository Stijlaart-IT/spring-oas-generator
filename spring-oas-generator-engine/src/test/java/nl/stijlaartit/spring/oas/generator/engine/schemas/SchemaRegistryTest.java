package nl.stijlaartit.spring.oas.generator.engine.schemas;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaRegistryTest {

    @Test
    void collectsComponentRootSchema() {
        Schema<?> user = new ObjectSchema()
                .addProperty("name", new StringSchema());

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("User", user)));

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        SchemaInstance instance = findBySchema(registry, user);
        assertNotNull(instance);

        assertInstanceOf(PathRoot.ComponentSchema.class, instance.path().root());
        PathRoot.ComponentSchema root = (PathRoot.ComponentSchema) instance.path().root();
        assertEquals("User", root.name());
    }

    @Test
    void collectsNestedComponentSchemas() {
        Schema<?> address = new ObjectSchema()
                .addProperty("city", new StringSchema());
        Schema<?> user = new ObjectSchema()
                .addProperty("address", address);

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("User", user)));

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        SchemaInstance addressInstance = findBySchema(registry, address);
        assertNotNull(addressInstance);
        assertInstanceOf(PathRoot.ComponentSchema.class, addressInstance.path().root());
        PathRoot.ComponentSchema root = (PathRoot.ComponentSchema) addressInstance.path().root();
        assertSame("User", root.name());
    }

    @Test
    void collectsAllOfSchemas() {
        Schema<?> partA = new ObjectSchema().addProperty("a", new StringSchema());
        Schema<?> partB = new ObjectSchema().addProperty("b", new StringSchema());
        Schema<?> wrapper = new Schema<>().allOf(List.of(partA, partB));

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Wrapper", wrapper)));

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        assertNotNull(findBySchema(registry, partA));
        assertNotNull(findBySchema(registry, partB));
    }

    @Test
    void collectsOneOfSchemas() {
        Schema<?> optionA = new ObjectSchema().addProperty("a", new StringSchema());
        Schema<?> optionB = new ObjectSchema().addProperty("b", new StringSchema());
        Schema<?> wrapper = new Schema<>().oneOf(List.of(optionA, optionB));

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Wrapper", wrapper)));

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        assertNotNull(findBySchema(registry, optionA));
        assertNotNull(findBySchema(registry, optionB));
    }

    @Test
    void collectsAnyOfSchemas() {
        Schema<?> optionA = new ObjectSchema().addProperty("a", new StringSchema());
        Schema<?> optionB = new ObjectSchema().addProperty("b", new StringSchema());
        Schema<?> wrapper = new Schema<>().anyOf(List.of(optionA, optionB));

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Wrapper", wrapper)));

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        assertNotNull(findBySchema(registry, optionA));
        assertNotNull(findBySchema(registry, optionB));
    }

    @Test
    void collectsArrayItemsSchemas() {
        Schema<?> item = new ObjectSchema().addProperty("id", new StringSchema());
        Schema<?> wrapper = new ArraySchema().items(item);

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Wrapper", wrapper)));

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        assertNotNull(findBySchema(registry, item));
    }

    @Test
    void collectsAdditionalPropertiesSchemas() {
        Schema<?> additional = new ObjectSchema().addProperty("meta", new StringSchema());
        Schema<?> wrapper = new ObjectSchema().additionalProperties(additional);

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Wrapper", wrapper)));

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        assertNotNull(findBySchema(registry, additional));
    }

    @Test
    void collectsRequestBodySchemas() {
        Schema<?> requestSchema = new ObjectSchema()
                .addProperty("name", new StringSchema());
        RequestBody requestBody = new RequestBody()
                .content(new Content().addMediaType("application/json", new MediaType().schema(requestSchema)));

        Operation operation = new Operation().requestBody(requestBody);
        OpenAPI openAPI = openApiWithOperation("/users", operation);

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        SchemaInstance instance = findBySchema(registry, requestSchema);
        assertNotNull(instance);
        assertInstanceOf(PathRoot.RequestBody.class, instance.path().root());
        PathRoot.RequestBody root = (PathRoot.RequestBody) instance.path().root();
        assertThat(root.operationName()).isEqualTo(OperationName.pathAndMethod("/users", HttpMethod.POST));
    }

    @Test
    void collectsResponseBodySchemasWithStatus() {
        Schema<?> responseSchema = new ObjectSchema()
                .addProperty("status", new StringSchema());
        ApiResponses responses = new ApiResponses()
                .addApiResponse("404", new ApiResponse()
                        .content(new Content().addMediaType("application/json",
                                new MediaType().schema(responseSchema))));

        Operation operation = new Operation().responses(responses);
        OpenAPI openAPI = openApiWithOperation("/users", operation);

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        SchemaInstance instance = findBySchema(registry, responseSchema);
        assertNotNull(instance);
        assertInstanceOf(PathRoot.ResponseBody.class, instance.path().root());
        PathRoot.ResponseBody root =
                (PathRoot.ResponseBody) instance.path().root();
        assertThat(OperationName.pathAndMethod("/users", HttpMethod.POST)).isEqualTo(root.operationName());
    }

    @Test
    void prefersApplicationJsonButFallsBackToFirstMediaType() {
        Schema<?> textSchema = new ObjectSchema().addProperty("text", new StringSchema());
        Content content = new Content().addMediaType("text/plain", new MediaType().schema(textSchema));

        Operation operation = new Operation()
                .requestBody(new RequestBody().content(content));

        OpenAPI openAPI = openApiWithOperation("/text", operation);

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        assertNotNull(findBySchema(registry, textSchema));
    }

    @Test
    void ignoresNullComponentSchemas() {
        Components components = new Components();
        java.util.Map<String, Schema> schemas = new java.util.LinkedHashMap<>();
        schemas.put("NullSchema", null);
        components.setSchemas(schemas);
        OpenAPI openAPI = new OpenAPI().components(components);

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);

        assertTrue(registry.getInstances().isEmpty());
    }

    @Test
    void throwsOnReferenceCycle() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("self", schema);
        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Self", schema)));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> SchemaRegistry.resolve(openAPI));
        assertTrue(ex.getMessage().contains("cycle"));
    }

    private static OpenAPI openApiWithOperation(String path, Operation operation) {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();
        PathItem pathItem = new PathItem().post(operation);
        paths.addPathItem(path, pathItem);
        openAPI.setPaths(paths);
        return openAPI;
    }

    private static SchemaInstance findBySchema(SchemaRegistry registry, Schema<?> schema) {
        return registry.getInstances().stream()
                .filter(instance -> instance.schema() == schema)
                .findFirst()
                .orElse(null);
    }
}
