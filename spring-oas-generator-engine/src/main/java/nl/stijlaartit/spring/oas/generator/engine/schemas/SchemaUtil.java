package nl.stijlaartit.spring.oas.generator.engine.schemas;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.jspecify.annotations.Nullable;

public class SchemaUtil {

    @Nullable
    public static String schemaTypeName(Schema<?> schema) {
        String type = schema.getType();
        if (type != null) {
            return type;
        }
        if (schema.getTypes() != null && !schema.getTypes().isEmpty()) {
            if (schema.getTypes().contains("string")) {
                return "string";
            }
            if (schema.getTypes().contains("integer")) {
                return "integer";
            }
            if (schema.getTypes().contains("number")) {
                return "number";
            }
            if (schema.getTypes().contains("boolean")) {
                return "boolean";
            }
            if (schema.getTypes().contains("array")) {
                return "array";
            }
            if (schema.getTypes().contains("object")) {
                return "object";
            }
            return schema.getTypes().iterator().next();
        }
        return switch (schema) {
            case StringSchema ignored -> "string";
            case IntegerSchema ignored -> "integer";
            case NumberSchema ignored -> "number";
            case BooleanSchema ignored -> "boolean";
            case ArraySchema ignored -> "array";
            case ObjectSchema ignored -> "object";
            default -> null;
        };
    }
}
