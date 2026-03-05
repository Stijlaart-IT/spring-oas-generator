package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBinarySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBooleanSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleLongSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleNumberSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaTypeResolverTest {

    @Test
    void resolvesStringSchemaToStringType() {
        SchemaType type = resolveSingle(new SimpleStringSchema(false), "User");
        assertInstanceOf(StringSchemaType.class, type);
    }

    @Test
    void resolvesIntegerSchemaToIntegerType() {
        SchemaType type = resolveSingle(new SimpleIntegerSchema(false), "User");
        assertInstanceOf(IntegerSchemaType.class, type);
    }

    @Test
    void resolvesNumberSchemaToDecimalType() {
        SchemaType type = resolveSingle(new SimpleNumberSchema(false), "User");
        assertInstanceOf(DecimalSchemaType.class, type);
    }

    @Test
    void resolvesLongSchemaToLongType() {
        SchemaType type = resolveSingle(new SimpleLongSchema(false), "User");
        assertInstanceOf(LongSchemaType.class, type);
    }

    @Test
    void resolvesBooleanSchemaToBooleanType() {
        SchemaType type = resolveSingle(new SimpleBooleanSchema(false), "User");
        assertInstanceOf(BooleanSchemaType.class, type);
    }

    @Test
    void resolvesBinarySchemaToBinaryType() {
        SchemaType type = resolveSingle(new SimpleBinarySchema(false), "FileContent");
        assertInstanceOf(BinarySchemaType.class, type);
    }

    @Test
    void resolvesStringEnumSchemaToEnumType() {
        SchemaType type = resolveSingle(new StringEnumSchema(false, List.of("A", "B")), "Status");
        assertInstanceOf(EnumSchemaType.class, type);
    }

    @Test
    void resolvesIntegerEnumSchemaToEnumType() {
        SchemaType type = resolveSingle(new IntegerEnumSchema(false, List.of(1, 2)), "Status");
        assertInstanceOf(EnumSchemaType.class, type);
    }

    @Test
    void resolvesNumberEnumSchemaToEnumType() {
        SchemaType type = resolveSingle(new NumberEnumSchema(false, List.of(new BigDecimal("1.5"), new BigDecimal("2.5"))), "Status");
        assertInstanceOf(EnumSchemaType.class, type);
    }

    @Test
    void resolvesArraySchemaToListTypeWithItemInstance() {
        SchemaType type = resolveSingle(new SimpleArraySchema(false, new SimpleStringSchema(false)), "User");

        assertInstanceOf(ListSchemaType.class, type);
        ListSchemaType listType = (ListSchemaType) type;
        assertNotNull(listType.itemInstance());
        assertInstanceOf(SimpleStringSchema.class, listType.itemInstance().schema());
    }

    @Test
    void resolvesMapSchemaToGeneratedObjectTypeForComponentMap() {
        SchemaType type = resolveSingle(new SimpleObjectSchema(false, List.of(), Set.of(), Optional.of(new SimpleStringSchema(false))), "User");

        assertInstanceOf(ObjectSchemaType.class, type);
        ObjectSchemaType objectType = (ObjectSchemaType) type;
        assertEquals(new JavaTypeName.Generated("User"), objectType.name());
    }

    @Test
    void resolvesObjectSchemaToGeneratedObjectTypeWithName() {
        SchemaType type = resolveSingle(new SimpleObjectSchema(false, List.of(), Set.of(), Optional.empty()), "User");

        assertInstanceOf(ObjectSchemaType.class, type);
        ObjectSchemaType objectType = (ObjectSchemaType) type;
        assertEquals(new JavaTypeName.Generated("User"), objectType.name());
    }

    @Test
    void resolvesUnionSchemaToUnionTypeWithVariantInstances() {
        SimpleSchema schema = new nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema(
                false,
                List.of(new SimpleStringSchema(false), new SimpleIntegerSchema(false)),
                "type"
        );

        SchemaType type = resolveSingle(schema, "UnionModel");
        assertInstanceOf(UnionSchemaType.class, type);

        UnionSchemaType unionType = (UnionSchemaType) type;
        assertEquals(2, unionType.variantInstances().size());
        assertEquals("type", unionType.discriminatorProperty());
        assertTrue(unionType.variantInstances().stream().anyMatch(instance -> instance.schema() instanceof SimpleStringSchema));
        assertTrue(unionType.variantInstances().stream().anyMatch(instance -> instance.schema() instanceof SimpleIntegerSchema));
    }

    @Test
    void doesNotGroupsInstancesWithEqualSchemaWhenBothAreRootComponents() {
        SimpleSchema shared = new SimpleObjectSchema(false, List.of(), Set.of(), Optional.empty());

        SchemaTypes types = resolveMultiple(new LinkedHashMap<>(Map.of(
                "User", shared,
                "UserAlias", shared
        )));

        SchemaType type = types.resolveFromSchema(shared);
        assertNotNull(type);
        assertEquals(1, type.instances().size());
    }

    @Test
    void resolvesAllOfMultiToCompositeType() {
        SimpleSchema schema = new CompositeSchema(false, List.of(
                new SimpleObjectSchema(false, List.of(), Set.of(), Optional.empty()),
                new SimpleObjectSchema(false, List.of(), Set.of(), Optional.empty())
        ));

        SchemaType type = resolveSingle(schema, "Composed");
        assertInstanceOf(CompositeSchemaType.class, type);
    }

    @Test
    void nameUniquenessAddsSuffix() {
        Map<String, SimpleSchema> schemas = new LinkedHashMap<>();
        schemas.put("user", new SimpleObjectSchema(false, List.of(), Set.of(), Optional.empty()));
        schemas.put("User", new SimpleObjectSchema(false, List.of(), Set.of(), Optional.empty()));

        SchemaTypes types = resolveMultiple(schemas);
        Map<JavaTypeName, GeneratedSchemaType> generated = types.generatedSchemaTypes().stream()
                .collect(Collectors.toMap(GeneratedSchemaType::name, type -> type));

        assertThat(generated).containsKey(new JavaTypeName.Generated("User"));
    }

    private SchemaType resolveSingle(SimpleSchema schema, String name) {
        SchemaTypes types = resolveMultiple(Map.of(name, schema));
        SchemaType type = types.resolveFromSchema(schema);
        assertNotNull(type);
        return type;
    }

    private SchemaTypes resolveMultiple(Map<String, SimpleSchema> schemas) {
        SimplifiedOas simplifiedOas = new SimplifiedOas(
                schemas,
                Map.of(),
                Map.of(),
                List.of(),
                Map.of()
        );
        SchemaRegistry registry = SchemaRegistry.resolve(simplifiedOas);
        return new SchemaTypeResolver(registry, NameProvider.create(), Logger.noOp()).resolve();
    }
}
