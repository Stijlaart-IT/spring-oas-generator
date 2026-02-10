package nl.stijlaartit.generation.model;

import nl.stijlaartit.generation.model.FieldDescriptor;
import nl.stijlaartit.generation.model.ModelDescriptor;
import nl.stijlaartit.generator.model.TypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelResolverTest {

    private ModelResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ModelResolver();
    }

    private OpenAPI openAPIWith(Map<String, Schema> schemas) {
        OpenAPI openAPI = new OpenAPI();
        Components components = new Components();
        components.setSchemas(schemas);
        openAPI.setComponents(components);
        return openAPI;
    }

    private OpenAPI openApiWithOperation(
            String path, String method, String operationId,
            RequestBody requestBody, ApiResponses responses) {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        Operation operation = new Operation()
                .operationId(operationId)
                .requestBody(requestBody)
                .responses(responses != null ? responses : new ApiResponses());

        PathItem pathItem = new PathItem();
        switch (method) {
            case "get" -> pathItem.setGet(operation);
            case "post" -> pathItem.setPost(operation);
            case "put" -> pathItem.setPut(operation);
            case "delete" -> pathItem.setDelete(operation);
            case "patch" -> pathItem.setPatch(operation);
        }
        paths.addPathItem(path, pathItem);
        openAPI.setPaths(paths);

        return openAPI;
    }

    @Nested
    class SimpleComponentSchemas {

        @Test
        void resolvesFlatObjectSchema() {
            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema())
                    .addProperty("age", new IntegerSchema())
                    .addRequiredItem("name");

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", userSchema)));

            assertEquals(1, models.size());
            RecordDescriptor user = (RecordDescriptor) models.get(0);
            assertEquals("User", user.name());
            assertEquals(2, user.fields().size());

            FieldDescriptor nameField = user.fields().stream()
                    .filter(f -> f.name().equals("name")).findFirst().orElseThrow();
            assertEquals("name", nameField.jsonName());
            assertEquals(TypeDescriptor.simple("java.lang.String"), nameField.type());
            assertTrue(nameField.required());

            FieldDescriptor ageField = user.fields().stream()
                    .filter(f -> f.name().equals("age")).findFirst().orElseThrow();
            assertEquals("age", ageField.jsonName());
            assertEquals(TypeDescriptor.simple("java.lang.Integer"), ageField.type());
            assertFalse(ageField.required());
        }

        @Test
        void resolvesMultipleComponentSchemas() {
            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema());
            Schema<?> petSchema = new ObjectSchema()
                    .addProperty("species", new StringSchema());

            Map<String, Schema> schemas = new LinkedHashMap<>();
            schemas.put("User", userSchema);
            schemas.put("Pet", petSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(schemas));

            assertEquals(2, models.size());
            assertEquals("User", models.get(0).name());
            assertEquals("Pet", models.get(1).name());
        }
    }

    @Nested
    class AnonymousNestedObjects {

        @Test
        void generatesModelForAnonymousNestedObject() {
            Schema<?> addressSchema = new ObjectSchema()
                    .addProperty("street", new StringSchema())
                    .addProperty("city", new StringSchema());

            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema())
                    .addProperty("address", addressSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", userSchema)));

            assertEquals(2, models.size());

            RecordDescriptor user = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();
            RecordDescriptor userAddress = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("UserAddress")).findFirst().orElseThrow();

            // User.address should reference UserAddress
            FieldDescriptor addressField = user.fields().stream()
                    .filter(f -> f.name().equals("address")).findFirst().orElseThrow();
            assertEquals(TypeDescriptor.complex("UserAddress"), addressField.type());

            // UserAddress should have street and city
            assertEquals(2, userAddress.fields().size());
            assertTrue(userAddress.fields().stream().anyMatch(f -> f.name().equals("street")));
            assertTrue(userAddress.fields().stream().anyMatch(f -> f.name().equals("city")));
        }

        @Test
        void generatesModelForDeeplyNestedAnonymousObject() {
            Schema<?> coordinatesSchema = new ObjectSchema()
                    .addProperty("lat", new StringSchema())
                    .addProperty("lng", new StringSchema());

            Schema<?> addressSchema = new ObjectSchema()
                    .addProperty("street", new StringSchema())
                    .addProperty("coordinates", coordinatesSchema);

            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema())
                    .addProperty("address", addressSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", userSchema)));

            assertEquals(3, models.size());
            assertTrue(models.stream().anyMatch(m -> m.name().equals("User")));
            assertTrue(models.stream().anyMatch(m -> m.name().equals("UserAddress")));
            assertTrue(models.stream().anyMatch(m -> m.name().equals("UserAddressCoordinates")));
        }
    }

    @Nested
    class Deduplication {

        @Test
        void reusesSameModelForAnonymousObjectsWithSameShape() {
            Schema<?> address1 = new ObjectSchema()
                    .addProperty("street", new StringSchema())
                    .addProperty("city", new StringSchema());

            Schema<?> address2 = new ObjectSchema()
                    .addProperty("street", new StringSchema())
                    .addProperty("city", new StringSchema());

            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema())
                    .addProperty("homeAddress", address1)
                    .addProperty("workAddress", address2);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", userSchema)));

            // Should be 2 models: User and UserHomeAddress (workAddress reuses UserHomeAddress)
            assertEquals(2, models.size());
            assertTrue(models.stream().anyMatch(m -> m.name().equals("User")));
            assertTrue(models.stream().anyMatch(m -> m.name().equals("UserHomeAddress")));

            // Both address fields should reference the same model
            RecordDescriptor user = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();

            FieldDescriptor homeAddr = user.fields().stream()
                    .filter(f -> f.name().equals("homeAddress")).findFirst().orElseThrow();
            FieldDescriptor workAddr = user.fields().stream()
                    .filter(f -> f.name().equals("workAddress")).findFirst().orElseThrow();

            assertEquals(homeAddr.type(), workAddr.type());
        }

        @Test
        void prefersComponentNameOverAnonymousName() {
            // Define Address as a component schema
            Schema<?> componentAddress = new ObjectSchema()
                    .addProperty("street", new StringSchema())
                    .addProperty("city", new StringSchema());

            // User has an anonymous inline object with the same shape
            Schema<?> inlineAddress = new ObjectSchema()
                    .addProperty("street", new StringSchema())
                    .addProperty("city", new StringSchema());

            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema())
                    .addProperty("address", inlineAddress);

            Map<String, Schema> schemas = new LinkedHashMap<>();
            schemas.put("Address", componentAddress);
            schemas.put("User", userSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(schemas));

            // Should have Address and User (no UserAddress because Address has same shape)
            assertEquals(2, models.size());
            assertTrue(models.stream().anyMatch(m -> m.name().equals("Address")));
            assertTrue(models.stream().anyMatch(m -> m.name().equals("User")));

            // User.address should reference Address (the component name)
            RecordDescriptor user = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();
            FieldDescriptor addressField = user.fields().stream()
                    .filter(f -> f.name().equals("address")).findFirst().orElseThrow();
            assertEquals(TypeDescriptor.complex("Address"), addressField.type());
        }

        @Test
        void doesNotDeduplicateTwoComponentSchemasWithSameShape() {
            Schema<?> categorySchema = new ObjectSchema()
                    .addProperty("id", new IntegerSchema().format("int64"))
                    .addProperty("name", new StringSchema());

            Schema<?> tagSchema = new ObjectSchema()
                    .addProperty("id", new IntegerSchema().format("int64"))
                    .addProperty("name", new StringSchema());

            Map<String, Schema> schemas = new LinkedHashMap<>();
            schemas.put("Category", categorySchema);
            schemas.put("Tag", tagSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(schemas));

            assertEquals(2, models.size());
            assertTrue(models.stream().anyMatch(m -> m.name().equals("Category")));
            assertTrue(models.stream().anyMatch(m -> m.name().equals("Tag")));
        }
    }

    @Nested
    class OperationSchemas {

        @Test
        void generatesModelForInlineRequestAndResponseBodies() {
            Schema<?> requestSchema = new ObjectSchema()
                    .addProperty("ids", new ArraySchema().items(new StringSchema()));
            RequestBody requestBody = new RequestBody()
                    .content(new Content()
                            .addMediaType("application/json",
                                    new MediaType().schema(requestSchema)));

            Schema<?> responseSchema = new ObjectSchema()
                    .addProperty("status", new StringSchema());
            ApiResponses responses = new ApiResponses()
                    .addApiResponse("200", new ApiResponse()
                            .content(new Content()
                                    .addMediaType("application/json",
                                            new MediaType().schema(responseSchema))));

            OpenAPI openAPI = openApiWithOperation(
                    "/me/albums", "put", "save-albums-user",
                    requestBody, responses
            );

            List<ModelDescriptor> models = resolver.resolve(openAPI);

            assertTrue(models.stream().anyMatch(m -> m.name().equals("SaveAlbumsUserRequest")));
            assertTrue(models.stream().anyMatch(m -> m.name().equals("SaveAlbumsUserResponse")));
        }
    }

    @Nested
    class TypeMapping {

        @Test
        void mapsStringTypes() {
            assertEquals(TypeDescriptor.simple("java.lang.String"),
                    ModelResolver.mapSimpleType("string", null));
            assertEquals(TypeDescriptor.simple("java.time.LocalDate"),
                    ModelResolver.mapSimpleType("string", "date"));
            assertEquals(TypeDescriptor.simple("java.time.OffsetDateTime"),
                    ModelResolver.mapSimpleType("string", "date-time"));
            assertEquals(TypeDescriptor.simple("java.util.UUID"),
                    ModelResolver.mapSimpleType("string", "uuid"));
        }

        @Test
        void mapsIntegerTypes() {
            assertEquals(TypeDescriptor.simple("java.lang.Integer"),
                    ModelResolver.mapSimpleType("integer", null));
            assertEquals(TypeDescriptor.simple("java.lang.Integer"),
                    ModelResolver.mapSimpleType("integer", "int32"));
            assertEquals(TypeDescriptor.simple("java.lang.Long"),
                    ModelResolver.mapSimpleType("integer", "int64"));
        }

        @Test
        void mapsNumberTypes() {
            assertEquals(TypeDescriptor.simple("java.math.BigDecimal"),
                    ModelResolver.mapSimpleType("number", null));
            assertEquals(TypeDescriptor.simple("java.lang.Float"),
                    ModelResolver.mapSimpleType("number", "float"));
            assertEquals(TypeDescriptor.simple("java.lang.Double"),
                    ModelResolver.mapSimpleType("number", "double"));
        }

        @Test
        void mapsBooleanType() {
            assertEquals(TypeDescriptor.simple("java.lang.Boolean"),
                    ModelResolver.mapSimpleType("boolean", null));
        }

        @Test
        void mapsTypeFromTypesSet() {
            Schema<?> schema = new Schema<>().types(Set.of("string"));
            TypeDescriptor type = resolver.resolveType("Parent", "name", schema);
            assertEquals(TypeDescriptor.simple("java.lang.String"), type);
        }

        @Test
        void mapsArrayType() {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setItems(new StringSchema());

            TypeDescriptor type = resolver.resolveType("Parent", "items", arraySchema);
            assertEquals(TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), type);
        }

        @Test
        void mapsMapType() {
            ObjectSchema mapSchema = new ObjectSchema();
            mapSchema.setAdditionalProperties(new IntegerSchema());

            TypeDescriptor type = resolver.resolveType("Parent", "metadata", mapSchema);
            assertEquals(TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")), type);
        }

        @Test
        void mapsMapTypeWithAdditionalPropertiesTrue() {
            ObjectSchema mapSchema = new ObjectSchema();
            mapSchema.setAdditionalProperties(true);

            TypeDescriptor type = resolver.resolveType("Parent", "metadata", mapSchema);
            assertEquals(TypeDescriptor.map(TypeDescriptor.simple("java.lang.Object")), type);
        }

        @Test
        void mapsRefType() {
            Schema<?> refSchema = new Schema<>();
            refSchema.set$ref("#/components/schemas/Pet");

            TypeDescriptor type = resolver.resolveType("Parent", "pet", refSchema);
            assertEquals(TypeDescriptor.complex("Pet"), type);
        }
    }

    @Nested
    class NamingConventions {

        @Test
        void convertsCamelCase() {
            assertEquals("firstName", ModelResolver.toCamelCase("first_name"));
            assertEquals("firstName", ModelResolver.toCamelCase("first-name"));
            assertEquals("name", ModelResolver.toCamelCase("name"));
        }

        @Test
        void convertsPascalCase() {
            assertEquals("FirstName", ModelResolver.toPascalCase("first_name"));
            assertEquals("FirstName", ModelResolver.toPascalCase("first-name"));
            assertEquals("Name", ModelResolver.toPascalCase("name"));
        }

        @Test
        void preservesJsonPropertyName() {
            Schema<?> schema = new ObjectSchema()
                    .addProperty("first_name", new StringSchema());

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", schema)));

            FieldDescriptor field = ((RecordDescriptor) models.get(0)).fields().get(0);
            assertEquals("firstName", field.name());
            assertEquals("first_name", field.jsonName());
        }

        @Test
        void sanitizesKeywordPropertyName() {
            Schema<?> schema = new ObjectSchema()
                    .addProperty("public", new BooleanSchema());

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("Playlist", schema)));

            FieldDescriptor field = ((RecordDescriptor) models.get(0)).fields().get(0);
            assertEquals("public_", field.name());
            assertEquals("public", field.jsonName());
        }
    }

    @Nested
    class Dependencies {

        @Test
        void tracksDependenciesOnOtherModels() {
            Schema<?> addressSchema = new ObjectSchema()
                    .addProperty("street", new StringSchema());

            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema())
                    .addProperty("address", addressSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", userSchema)));

            RecordDescriptor user = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();

            assertEquals(List.of("UserAddress"), user.dependencies());
        }

        @Test
        void tracksRefDependencies() {
            Schema<?> petRef = new Schema<>();
            petRef.set$ref("#/components/schemas/Pet");

            Schema<?> ownerSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema())
                    .addProperty("pet", petRef);

            Schema<?> petSchema = new ObjectSchema()
                    .addProperty("species", new StringSchema());

            Map<String, Schema> schemas = new LinkedHashMap<>();
            schemas.put("Pet", petSchema);
            schemas.put("Owner", ownerSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(schemas));

            RecordDescriptor owner = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("Owner")).findFirst().orElseThrow();

            assertEquals(List.of("Pet"), owner.dependencies());
        }
    }

    @Nested
    class Enums {

        @Test
        void resolvesComponentEnumSchema() {
            StringSchema statusSchema = new StringSchema();
            statusSchema.setEnum(List.of("available", "pending", "sold"));

            Schema<?> orderSchema = new ObjectSchema()
                    .addProperty("status", new Schema<>().$ref("#/components/schemas/Status"));

            Map<String, Schema> schemas = new LinkedHashMap<>();
            schemas.put("Status", statusSchema);
            schemas.put("Order", orderSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(schemas));

            EnumDescriptor status = (EnumDescriptor) models.stream()
                    .filter(m -> m.name().equals("Status")).findFirst().orElseThrow();
            RecordDescriptor order = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("Order")).findFirst().orElseThrow();

            assertEquals(List.of("available", "pending", "sold"), status.enumValues());

            FieldDescriptor statusField = order.fields().stream()
                    .filter(f -> f.name().equals("status")).findFirst().orElseThrow();
            assertEquals(TypeDescriptor.complex("Status"), statusField.type());
        }

        @Test
        void resolvesInlineEnumSchema() {
            StringSchema statusSchema = new StringSchema();
            statusSchema.setEnum(List.of("active", "inactive"));

            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("status", statusSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", userSchema)));

            RecordDescriptor user = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();
            EnumDescriptor userStatus = (EnumDescriptor) models.stream()
                    .filter(m -> m.name().equals("UserStatus")).findFirst().orElseThrow();

            FieldDescriptor statusField = user.fields().stream()
                    .filter(f -> f.name().equals("status")).findFirst().orElseThrow();
            assertEquals(TypeDescriptor.complex("UserStatus"), statusField.type());
            assertEquals(List.of("active", "inactive"), userStatus.enumValues());
        }

        @Test
        void reusesAnonymousEnumsWithSameValues() {
            StringSchema primarySchema = new StringSchema();
            primarySchema.setEnum(List.of("open", "closed"));
            StringSchema secondarySchema = new StringSchema();
            secondarySchema.setEnum(List.of("open", "closed"));

            Schema<?> userSchema = new ObjectSchema()
                    .addProperty("primaryStatus", primarySchema)
                    .addProperty("secondaryStatus", secondarySchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(Map.of("User", userSchema)));

            RecordDescriptor user = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();

            FieldDescriptor primary = user.fields().stream()
                    .filter(f -> f.name().equals("primaryStatus")).findFirst().orElseThrow();
            FieldDescriptor secondary = user.fields().stream()
                    .filter(f -> f.name().equals("secondaryStatus")).findFirst().orElseThrow();

            assertEquals(primary.type(), secondary.type());
        }
    }

    @Nested
    class OneOf {

        @Test
        void resolvesOneOfAsInterfaceWithImplementations() {
            Schema<?> trackSchema = new ObjectSchema()
                    .addProperty("name", new StringSchema());
            StringSchema trackType = new StringSchema();
            trackType.setEnum(List.of("track"));
            trackSchema.addProperty("type", trackType);
            Schema<?> episodeSchema = new ObjectSchema()
                    .addProperty("title", new StringSchema());
            StringSchema episodeType = new StringSchema();
            episodeType.setEnum(List.of("episode"));
            episodeSchema.addProperty("type", episodeType);

            Schema<?> oneOfSchema = new Schema<>();
            oneOfSchema.setOneOf(List.of(
                    new Schema<>().$ref("#/components/schemas/TrackObject"),
                    new Schema<>().$ref("#/components/schemas/EpisodeObject")
            ));

            Schema<?> queueSchema = new ObjectSchema()
                    .addProperty("currently_playing", oneOfSchema);

            Map<String, Schema> schemas = new LinkedHashMap<>();
            schemas.put("TrackObject", trackSchema);
            schemas.put("EpisodeObject", episodeSchema);
            schemas.put("QueueObject", queueSchema);

            List<ModelDescriptor> models = resolver.resolve(openAPIWith(schemas));

            RecordDescriptor queue = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("QueueObject")).findFirst().orElseThrow();
            FieldDescriptor currentlyPlaying = queue.fields().stream()
                    .filter(f -> f.name().equals("currentlyPlaying")).findFirst().orElseThrow();
            assertEquals(TypeDescriptor.complex("QueueObjectCurrentlyPlaying"), currentlyPlaying.type());

            OneOfDescriptor oneOf = (OneOfDescriptor) models.stream()
                    .filter(m -> m.name().equals("QueueObjectCurrentlyPlaying")).findFirst().orElseThrow();
            assertEquals(
                    List.of("TrackObject", "EpisodeObject"),
                    oneOf.variants().stream().map(OneOfDescriptor.OneOfVariant::modelName).toList()
            );
            assertEquals(
                    List.of("track", "episode"),
                    oneOf.variants().stream().map(OneOfDescriptor.OneOfVariant::discriminatorValue).toList()
            );

            RecordDescriptor track = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("TrackObject")).findFirst().orElseThrow();
            RecordDescriptor episode = (RecordDescriptor) models.stream()
                    .filter(m -> m.name().equals("EpisodeObject")).findFirst().orElseThrow();

            assertEquals(List.of("QueueObjectCurrentlyPlaying"), track.implementsTypes());
            assertEquals(List.of("QueueObjectCurrentlyPlaying"), episode.implementsTypes());
        }
    }
}
