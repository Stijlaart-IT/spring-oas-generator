package nl.stijlaartit.generator.engine.model;

import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Objects;

/**
 * Structural signature of an enum schema, used to deduplicate anonymous enums.
 */
public record EnumSignature(String type, String format, List<String> values) {

    public static EnumSignature of(Schema<?> schema) {
        String type = Objects.toString(schema.getType(), "string");
        String format = schema.getFormat();
        List<String> values = schema.getEnum().stream()
                .map(String::valueOf)
                .toList();
        return new EnumSignature(type, format, List.copyOf(values));
    }
}
