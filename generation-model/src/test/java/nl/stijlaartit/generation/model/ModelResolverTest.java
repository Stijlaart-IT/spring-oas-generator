package nl.stijlaartit.generation.model;

import nl.stijlaartit.generation.model.FieldDescriptor;
import nl.stijlaartit.generation.model.ModelDescriptor;
import nl.stijlaartit.generator.model.TypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            ModelDescriptor user = models.get(0);
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

            ModelDescriptor user = models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();
            ModelDescriptor userAddress = models.stream()
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
            ModelDescriptor user = models.stream()
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
            ModelDescriptor user = models.stream()
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

            FieldDescriptor field = models.get(0).fields().get(0);
            assertEquals("firstName", field.name());
            assertEquals("first_name", field.jsonName());
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

            ModelDescriptor user = models.stream()
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

            ModelDescriptor owner = models.stream()
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

            ModelDescriptor status = models.stream()
                    .filter(m -> m.name().equals("Status")).findFirst().orElseThrow();
            ModelDescriptor order = models.stream()
                    .filter(m -> m.name().equals("Order")).findFirst().orElseThrow();

            assertTrue(status.isEnum());
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

            ModelDescriptor user = models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();
            ModelDescriptor userStatus = models.stream()
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

            ModelDescriptor user = models.stream()
                    .filter(m -> m.name().equals("User")).findFirst().orElseThrow();

            FieldDescriptor primary = user.fields().stream()
                    .filter(f -> f.name().equals("primaryStatus")).findFirst().orElseThrow();
            FieldDescriptor secondary = user.fields().stream()
                    .filter(f -> f.name().equals("secondaryStatus")).findFirst().orElseThrow();

            assertEquals(primary.type(), secondary.type());
        }
    }
}
