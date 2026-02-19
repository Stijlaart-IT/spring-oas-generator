package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SchemaTypeResolverTest {

    @Test
    void resolvesStringSchemaToStringType() {
        Schema<?> schema = new StringSchema();
        SchemaType type = resolveSingle(schema, "User");

        assertTrue(type instanceof StringSchemaType);
    }

    @Test
    void resolvesIntegerSchemaToIntegerType() {
        Schema<?> schema = new IntegerSchema();
        SchemaType type = resolveSingle(schema, "User");

        assertTrue(type instanceof IntegerSchemaType);
    }

    @Test
    void resolvesNumberSchemaToDecimalType() {
        Schema<?> schema = new NumberSchema();
        SchemaType type = resolveSingle(schema, "User");

        assertTrue(type instanceof DecimalSchemaType);
    }

    @Test
    void resolvesBooleanSchemaToBooleanType() {
        Schema<?> schema = new BooleanSchema();
        SchemaType type = resolveSingle(schema, "User");

        assertTrue(type instanceof BooleanSchemaType);
    }

    @Test
    void resolvesArraySchemaToListTypeWithItemInstance() {
        Schema<?> schema = new ArraySchema().items(new StringSchema());
        SchemaType type = resolveSingle(schema, "User");

        assertTrue(type instanceof ListSchemaType);
        ListSchemaType listType = (ListSchemaType) type;
        assertNotNull(listType.itemInstance());
        assertTrue(listType.itemInstance().schema() instanceof StringSchema);
    }

    @Test
    void resolvesMapSchemaToGeneratedObjectTypeForComponentMap() {
        Schema<?> schema = new ObjectSchema().additionalProperties(new StringSchema());
        SchemaType type = resolveSingle(schema, "User");

        assertTrue(type instanceof ObjectSchemaType);
        ObjectSchemaType objectType = (ObjectSchemaType) type;
        assertEquals("User", objectType.name());
    }

    @Test
    void resolvesObjectSchemaToGeneratedObjectTypeWithName() {
        Schema<?> schema = new ObjectSchema();
        SchemaType type = resolveSingle(schema, "User");

        assertTrue(type instanceof ObjectSchemaType);
        ObjectSchemaType objectType = (ObjectSchemaType) type;
        assertEquals("User", objectType.name());
    }

    @Test
    void resolvesEnumSchemaToGeneratedEnumTypeWithoutDedupe() {
        StringSchema enumA = new StringSchema();
        enumA.setEnum(List.of("A", "B"));
        StringSchema enumB = new StringSchema();
        enumB.setEnum(List.of("A", "C"));

        SchemaTypes types = resolveMultiple(Map.of(
                "Status", enumA,
                "State", enumB
        ));

        long enumCount = types.generatedSchemaTypes().stream().filter(EnumSchemaType.class::isInstance).count();
        assertEquals(2, enumCount);
    }

    @Test
    void resolvesUnionSchemaToUnionTypeWithVariantInstances() {
        Schema<?> schema = new ObjectSchema();
        schema.setOneOf(List.of(new StringSchema(), new IntegerSchema()));

        SchemaType type = resolveSingle(schema, "UnionModel");
        assertTrue(type instanceof UnionSchemaType);

        UnionSchemaType unionType = (UnionSchemaType) type;
        assertEquals(2, unionType.variantInstances().size());
        assertTrue(unionType.variantInstances().stream().anyMatch(instance -> instance.schema() instanceof StringSchema));
        assertTrue(unionType.variantInstances().stream().anyMatch(instance -> instance.schema() instanceof IntegerSchema));
    }

    @Test
    void groupsInstancesWithEqualSchema() {
        Schema<?> shared = new ObjectSchema();

        SchemaTypes types = resolveMultiple(new LinkedHashMap<>(Map.of(
                "User", shared,
                "UserAlias", shared
        )));

        SchemaType type = types.resolveFromSchema(shared);
        assertNotNull(type);
        assertEquals(2, type.instances().size());
    }

    @Test
    void nameUniquenessAddsSuffix() {
        Map<String, Schema<?>> schemas = new LinkedHashMap<>();
        schemas.put("user", new ObjectSchema());
        schemas.put("User", new ObjectSchema().addProperties("id", new StringSchema()));

        SchemaTypes types = resolveMultiple(schemas);
        Map<String, GeneratedSchemaType> generated = types.generatedSchemaTypes().stream()
                .collect(Collectors.toMap(GeneratedSchemaType::name, type -> type));

        assertTrue(generated.containsKey("User"));
        assertTrue(generated.containsKey("User2"));
    }

    private SchemaType resolveSingle(Schema<?> schema, String name) {
        SchemaTypes types = resolveMultiple(Map.of(name, schema));
        SchemaType type = types.resolveFromSchema(schema);
        assertNotNull(type);
        return type;
    }

    private SchemaTypes resolveMultiple(Map<String, Schema<?>> schemas) {
        OpenAPI openAPI = new OpenAPI();
        Components components = new Components();
        components.setSchemas(new LinkedHashMap<>(schemas));
        openAPI.setComponents(components);

        SchemaRegistry registry = SchemaRegistry.resolve(openAPI);
        return new SchemaTypeResolver().resolve(registry);
    }
}
