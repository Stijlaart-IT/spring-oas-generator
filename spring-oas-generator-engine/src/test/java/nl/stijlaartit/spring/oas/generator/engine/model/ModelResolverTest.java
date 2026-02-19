package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.engine.model.ModelResolver;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.domain.ModelFile;
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
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModelResolverTest {

    private List<ModelFile> resolveModels(OpenAPI openAPI) {
        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        ModelResolver resolver = new ModelResolver(registry);
        SchemaTypes schemaTypes = new SchemaTypeResolver().resolve(registry);
        return resolver.resolve(schemaTypes);
    }

    private OpenAPI openApiWithOperation(String path, Operation operation) {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        pathItem.setPost(operation);
        paths.addPathItem(path, pathItem);
        openAPI.setPaths(paths);
        return openAPI;
    }

    @Test
    void resolvesComponentPrimitiveSchema() {
        Schema<?> component = new StringSchema();
        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("user_name", component)));

        List<ModelFile> models = resolveModels(openAPI);

        assertEquals(0, models.size());
    }

    @Test
    void resolvesInlineObjectSchema() {
        Schema<?> requestSchema = new ObjectSchema()
                .addProperty("name", new StringSchema());
        RequestBody requestBody = new RequestBody()
                .content(new Content().addMediaType("application/json", new MediaType().schema(requestSchema)));
        Operation operation = new Operation().requestBody(requestBody);
        OpenAPI openAPI = openApiWithOperation("/users", operation);

        List<ModelFile> models = resolveModels(openAPI);

        assertEquals(1, models.size());
        assertEquals("PostUsersRequest", models.getFirst().name());
    }

    @Test
    void resolvesInlineArraySchema() {
        Schema<?> responseSchema = new ArraySchema().items(new StringSchema());
        ApiResponse response = new ApiResponse()
                .content(new Content().addMediaType("application/json", new MediaType().schema(responseSchema)));
        ApiResponses responses = new ApiResponses().addApiResponse("200", response);
        Operation operation = new Operation().responses(responses);
        OpenAPI openAPI = openApiWithOperation("/items", operation);

        List<ModelFile> models = resolveModels(openAPI);

        assertEquals(0, models.size());
    }

    @Test
    void resolvesInlineEnumSchema() {
        StringSchema enumSchema = new StringSchema();
        enumSchema.setEnum(List.of("A", "B"));
        RequestBody requestBody = new RequestBody()
                .content(new Content().addMediaType("application/json", new MediaType().schema(enumSchema)));
        Operation operation = new Operation().requestBody(requestBody);
        OpenAPI openAPI = openApiWithOperation("/enum", operation);

        List<ModelFile> models = resolveModels(openAPI);

        assertEquals(1, models.size());
        assertEquals("PostEnumRequest", models.getFirst().name());
    }

    @Test
    void resolvesOneOfSchemas() {
        StringSchema optionString = new StringSchema();
        optionString.setEnum(List.of("A", "B"));
        Schema<?> optionObject = new ObjectSchema().addProperty("id", new StringSchema());
        Schema<?> wrapper = new Schema<>().oneOf(List.of(optionString, optionObject));

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of("Wrapper", wrapper)));

        List<ModelFile> models = resolveModels(openAPI);

        assertEquals(3, models.size());
        assertTrue(models.stream().anyMatch(model -> model.name().equals("Wrapper")));
        assertEquals(3, models.stream().map(ModelFile::name).distinct().count());
    }
}
