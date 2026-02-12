package nl.stijlaartit.generator.engine.schemas;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SchemaUtilTest {

    @Test
    void usesExplicitTypeWhenSet() {
        Schema<?> schema = new Schema<>().type("custom");
        assertEquals("custom", SchemaUtil.schemaTypeName(schema));
    }

    @Test
    void usesTypesSetWhenTypeMissing() {
        Schema<?> schema = new Schema<>().types(Set.of("integer", "string"));
        assertEquals("string", SchemaUtil.schemaTypeName(schema));
    }

    @Test
    void infersTypeFromSchemaClass() {
        assertEquals("string", SchemaUtil.schemaTypeName(new StringSchema()));
        assertEquals("array", SchemaUtil.schemaTypeName(new ArraySchema()));
    }

    @Test
    void returnsNullWhenUnknown() {
        Schema<?> schema = new Schema<>();
        assertNull(SchemaUtil.schemaTypeName(schema));
    }
}
