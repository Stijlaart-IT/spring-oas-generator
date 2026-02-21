package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaInstanceGroup.determineSchemaGroupType;
import static org.assertj.core.api.Assertions.assertThat;

class SchemaInstanceGroupTest {

    @Test
    public void determineIntegerSchema() {
        assertThat(determineSchemaGroupType(new IntegerSchema())).isEqualTo(SchemaGroupType.INTEGER);
        assertThat(determineSchemaGroupType(new Schema<>().type("integer"))).isEqualTo(SchemaGroupType.INTEGER);
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("integer")))).isEqualTo(SchemaGroupType.INTEGER);
    }

    @Test
    public void determineNumberSchema() {
        assertThat(determineSchemaGroupType(new NumberSchema())).isEqualTo(SchemaGroupType.NUMBER);
        assertThat(determineSchemaGroupType(new Schema<>().type("number"))).isEqualTo(SchemaGroupType.NUMBER);
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("number")))).isEqualTo(SchemaGroupType.NUMBER);
    }

    @Test
    public void determineInvalidOnMultipleTypes() {
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("integer", "string"))))
                .isEqualTo(SchemaGroupType.INVALID);
    }

    @Test
    public void determineBooleanSchema() {
        assertThat(determineSchemaGroupType(new BooleanSchema())).isEqualTo(SchemaGroupType.BOOLEAN);
        assertThat(determineSchemaGroupType(new Schema<>().type("boolean"))).isEqualTo(SchemaGroupType.BOOLEAN);
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("boolean")))).isEqualTo(SchemaGroupType.BOOLEAN);
    }

    @Test
    public void determineStringSchema() {
        assertThat(determineSchemaGroupType(new StringSchema())).isEqualTo(SchemaGroupType.STRING);
        assertThat(determineSchemaGroupType(new Schema<>().type("string"))).isEqualTo(SchemaGroupType.STRING);
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("string")))).isEqualTo(SchemaGroupType.STRING);
    }

    @Test
    public void determineStringEnumSchema() {
        assertThat(determineSchemaGroupType(new StringSchema()._enum(List.of("red", "green")))).isEqualTo(SchemaGroupType.ENUM);
        assertThat(determineSchemaGroupType(new Schema<>().type("string")._enum(List.of("red", "green")))).isEqualTo(SchemaGroupType.ENUM);
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("string"))._enum(List.of("red", "green")))).isEqualTo(SchemaGroupType.ENUM);
    }

    @Test
    public void determineArraySchema() {
        assertThat(determineSchemaGroupType(new ArraySchema())).isEqualTo(SchemaGroupType.ARRAY);
        assertThat(determineSchemaGroupType(new Schema<>().type("array"))).isEqualTo(SchemaGroupType.ARRAY);
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("array")))).isEqualTo(SchemaGroupType.ARRAY);
    }

    @Test
    public void determineObjectSchema() {
        assertThat(determineSchemaGroupType(new ObjectSchema())).isEqualTo(SchemaGroupType.OBJECT);
        assertThat(determineSchemaGroupType(new Schema<>().type("object"))).isEqualTo(SchemaGroupType.OBJECT);
        assertThat(determineSchemaGroupType(new Schema<>().types(Set.of("object")))).isEqualTo(SchemaGroupType.OBJECT);
    }

    @Test
    public void determineRefSchema() {
        assertThat(determineSchemaGroupType(new Schema<>().$ref("User"))).isEqualTo(SchemaGroupType.REF);
    }

    @Test
    public void determineAllOfSchema() {
        assertThat(determineSchemaGroupType(new Schema<>().allOf(List.of()))).isEqualTo(SchemaGroupType.ALL_OF_EMPTY);
        assertThat(determineSchemaGroupType(new Schema<>().allOf(List.of(new ObjectSchema()))))
                .isEqualTo(SchemaGroupType.ALL_OF_SINGLE);
        assertThat(determineSchemaGroupType(new ObjectSchema().allOf(List.of(new ObjectSchema()))))
                .isEqualTo(SchemaGroupType.ALL_OF_SINGLE);
        assertThat(determineSchemaGroupType(new Schema<>().allOf(List.of(new ObjectSchema(), new StringSchema()))))
                .isEqualTo(SchemaGroupType.ALL_OF_MULTI);
    }

    @Test
    public void determineOneOfSchema() {
        assertThat(determineSchemaGroupType(new Schema<>().oneOf(List.of())))
                .isEqualTo(SchemaGroupType.ONE_OF_EMPTY);
        assertThat(determineSchemaGroupType(new Schema<>().oneOf(List.of(new ObjectSchema()))))
                .isEqualTo(SchemaGroupType.ONE_OF_SINGLE);
        assertThat(determineSchemaGroupType(new Schema<>().oneOf(List.of(new ObjectSchema(), new StringSchema()))))
                .isEqualTo(SchemaGroupType.ONE_OF_MULTI);
    }

    @Test
    public void determineAnyOfSchema() {
        assertThat(determineSchemaGroupType(new Schema<>().anyOf(List.of())))
                .isEqualTo(SchemaGroupType.ANY_OF_EMPTY);
        assertThat(determineSchemaGroupType(new Schema<>().anyOf(List.of(new ObjectSchema()))))
                .isEqualTo(SchemaGroupType.ANY_OF_SINGLE);
        assertThat(determineSchemaGroupType(new Schema<>().anyOf(List.of(new ObjectSchema(), new StringSchema()))))
                .isEqualTo(SchemaGroupType.ANY_OF_MULTI);
    }


    @Test
    public void determineEmptyComposedSchema() {
        assertThat(determineSchemaGroupType(new ComposedSchema())).isEqualTo(SchemaGroupType.EMPTY);
    }
}
