package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordField;
import nl.stijlaartit.spring.oas.generator.domain.file.ModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordModel;
import nl.stijlaartit.spring.oas.generator.domain.file.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.engine.OasSimplifier;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaParameterName;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schematype.CompositeSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.IntegerSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypeResolver;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.spring.oas.generator.engine.schematype.StringSchemaType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelResolverTest {

    private List<ModelFile> resolveModels(OpenAPI openAPI) {
        OasSimplifier oasSimplifier = new OasSimplifier(Logger.noOp());
        final SimplifiedOas simplifiedOas = oasSimplifier.simplify(openAPI);
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeDescriptorFactory typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes, "com.example.models");
        ModelResolver resolver = new ModelResolver(schemaTypes, typeDescriptorFactory, Logger.noOp());
        return resolver.resolve();
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

    @Test
    void resolvesOneOfSchemasWithDiscriminatorProperty() {
        Schema<?> wrapper = new Schema<>()
                .oneOf(List.of(
                        new Schema<>().$ref("#/components/schemas/Car"),
                        new Schema<>().$ref("#/components/schemas/Bike")
                ));
        wrapper.setDiscriminator(new Discriminator().propertyName("type"));

        StringSchema carType = new StringSchema();
        carType.setEnum(List.of("car"));
        Schema<?> car = new ObjectSchema()
                .addProperty("type", carType)
                .addProperty("brand", new StringSchema());
        StringSchema bikeType = new StringSchema();
        bikeType.setEnum(List.of("bike"));
        Schema<?> bike = new ObjectSchema()
                .addProperty("type", bikeType)
                .addProperty("model", new StringSchema());

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "Wrapper", wrapper,
                        "Car", car,
                        "Bike", bike
                )));

        List<ModelFile> models = resolveModels(openAPI);

        UnionModelFile union = models.stream()
                .filter(model -> model instanceof UnionModelFile)
                .map(model -> (UnionModelFile) model)
                .findFirst()
                .orElseThrow();
        assertThat(union.discriminatorProperty()).isEqualTo("type");
        assertThat(union.variants()).extracting(v -> v.discriminatorValue()).containsExactly("car", "bike");
    }

    @Test
    void resolvesPagingSchemas_allFieldsInBothModelsAreRequired() {
        Schema<?> pagingObject = new ObjectSchema()
                .required(List.of("href", "items", "limit", "next"))
                .addProperty("href", new StringSchema())
                .addProperty("limit", new IntegerSchema())
                .addProperty("next", new StringSchema().nullable(true));

        Schema<?> pagingThingObject = new Schema<>().allOf(List.of(
                new Schema<>().$ref("#/components/schemas/PagingObject"),
                new ObjectSchema()
                        .addProperty("items", new ArraySchema().items(new StringSchema()))
        ));

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "PagingObject", pagingObject,
                        "PagingThingObject", pagingThingObject
                )));

        List<ModelFile> models = resolveModels(openAPI);

        RecordModel paging = (RecordModel) models.stream()
                .filter(model -> model.name().equals("PagingObject"))
                .findFirst()
                .orElseThrow();
        RecordModel pagingThing = (RecordModel) models.stream()
                .filter(model -> model.name().equals("PagingThingObject"))
                .findFirst()
                .orElseThrow();

        assertThat(paging.fields()).isNotEmpty().allMatch(RecordField::required);
        assertThat(pagingThing.fields()).isNotEmpty().allMatch(RecordField::required);
    }

    @Test
    void resolvesPrimitiveComponentReferencedFromObject() {
        Schema<?> primitiveRef = new Schema<>();
        primitiveRef.set$ref("#/components/schemas/PrimitiveKey");
        Schema<?> objectWithRef = new ObjectSchema()
                .addProperty("key", primitiveRef)
                .addRequiredItem("key");

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "ObjectWithRefToPrimitive", objectWithRef,
                        "PrimitiveKey", new IntegerSchema()
                )));

        List<ModelFile> models = resolveModels(openAPI);

        assertTrue(models.stream().anyMatch(model -> model.name().equals("ObjectWithRefToPrimitive")));
        RecordModel first = (RecordModel) models.getFirst();
        final var field = first.fields().getFirst();
        assertThat(field.name().value()).isEqualTo("key");
        assertThat(field.type()).isEqualTo(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer")));
    }

    @Test
    void failsOnMixedAllOfType() {
        Schema<?> allOf = new Schema<>()
                .allOf(List.of(
                        new StringSchema(),
                        new IntegerSchema()
                ));

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "UseMixedAllOf", new ObjectSchema().addProperty("mixed", new Schema().$ref("#/components/schemas/MixedAllOf")),
                        "MixedAllOf", allOf
                )));

        OasSimplifier oasSimplifier = new OasSimplifier(Logger.noOp());
        final SimplifiedOas simplifiedOas = oasSimplifier.simplify(openAPI);
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeDescriptorFactory typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes, "com.example.models");
        ModelResolver resolver = new ModelResolver(schemaTypes, typeDescriptorFactory, Logger.noOp());
        List<ModelFile> models = resolver.resolve();

        assertThat(models).hasSize(1);
        assertThat(models.getFirst().name()).isEqualTo("UseMixedAllOf");
        assertThat(((RecordModel) models.getFirst()).fields()).hasSize(1);
        assertThat(((RecordModel) models.getFirst()).fields().getFirst().name()).isEqualTo(new JavaParameterName("mixed"));
        assertThat(((RecordModel) models.getFirst()).fields().getFirst().type()).isEqualTo(
                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"))
        );

        assertThat(schemaTypes.types()).hasSize(5);
    }

    @Test
    void shouldHaveStablePropertyOrderWithAllOf() {

        OpenAPI openAPI = new OpenAPI()
                .components(new Components().schemas(Map.of(
                        "PlaylistUserObject",
                        new ObjectSchema()
                                .addProperty("external_urls", new StringSchema())
                                .addProperty("href", new StringSchema())
                                .addProperty("id", new StringSchema())
                                .addProperty("type", new StringSchema())
                                .addProperty("uri", new StringSchema()),
                        "PlaylistOwnerObject",
                        new Schema().allOf(List.of(
                                new Schema().$ref("#/components/schemas/PlaylistUserObject"),
                                new ObjectSchema().addProperty("display_name", new StringSchema())
                        ))
                )));

        OasSimplifier oasSimplifier = new OasSimplifier(Logger.noOp());
        final SimplifiedOas simplifiedOas = oasSimplifier.simplify(openAPI);
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        SchemaTypes schemaTypes = new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
        TypeDescriptorFactory typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes, "com.example.models");
        ModelResolver resolver = new ModelResolver(schemaTypes, typeDescriptorFactory, Logger.noOp());
        List<ModelFile> models = resolver.resolve();

        assertEquals(3, models.size());
        final var playlistOwnerObject = (RecordModel) models.stream().filter(v -> v.name().equals("PlaylistOwnerObject")).findFirst().orElseThrow();
        final var fieldNames = playlistOwnerObject.fields()
                .stream().map(RecordField::name)
                .map(JavaParameterName::value)
                .toList();
        assertThat(fieldNames).isEqualTo(List.of(
                "externalUrls", "href", "id", "type", "uri", "displayName"
        ));
    }
}
