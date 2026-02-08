package com.example.generation.model;

import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Structural signature of a schema, used to detect when two schemas have the same shape.
 * Two schemas with the same signature should share a single model.
 */
record SchemaSignature(Map<String, String> fieldSignatures, Set<String> requiredFields) {

    static SchemaSignature of(Schema<?> schema) {
        Map<String, String> fields = new TreeMap<>();
        if (schema.getProperties() != null) {
            for (var entry : schema.getProperties().entrySet()) {
                fields.put(entry.getKey(), typeSignature(entry.getValue()));
            }
        }

        Set<String> required = schema.getRequired() != null
                ? Set.copyOf(schema.getRequired())
                : Set.of();

        return new SchemaSignature(Map.copyOf(fields), required);
    }

    private static String typeSignature(Schema<?> schema) {
        if (schema.get$ref() != null) {
            return "ref:" + schema.get$ref();
        }

        String type = Objects.toString(schema.getType(), "object");
        String format = schema.getFormat();

        if ("array".equals(type) && schema.getItems() != null) {
            return "array<" + typeSignature(schema.getItems()) + ">";
        }

        if ("object".equals(type) && schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            SchemaSignature nested = SchemaSignature.of(schema);
            return "object:" + nested;
        }

        if ("object".equals(type) && schema.getAdditionalProperties() instanceof Schema<?> additional) {
            return "map<" + typeSignature(additional) + ">";
        }

        return format != null ? type + ":" + format : type;
    }
}
