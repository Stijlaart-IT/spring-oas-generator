package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record SchemaInstanceGroup(Schema<?> schema, List<SchemaInstance> instances, SchemaGroupType groupType) {
    public SchemaInstanceGroup(Schema<?> schema, List<SchemaInstance> instances, SchemaGroupType groupType) {
        this.schema = Objects.requireNonNull(schema);
        this.instances = List.copyOf(instances);
        this.groupType = Objects.requireNonNull(groupType);
    }

    public static SchemaInstanceGroups groupBySchemaEquals(List<SchemaInstance> instances) {
        Map<Schema<?>, List<SchemaInstance>> grouped = new LinkedHashMap<>();
        for (SchemaInstance instance : instances) {
            Schema<?> schema = instance.schema();
            if (!grouped.containsKey(schema)) {
                grouped.put(schema, new ArrayList<>());
            }
            grouped.get(schema).add(instance);
        }

        List<SchemaInstanceGroup> groups = new ArrayList<>();
        for (Map.Entry<Schema<?>, List<SchemaInstance>> entry : grouped.entrySet()) {
            final var type = determineSchemaGroupType(entry.getKey());
            groups.add(new SchemaInstanceGroup(entry.getKey(), entry.getValue(), type));
        }
        return new SchemaInstanceGroups(groups);
    }

    public static SchemaGroupType determineSchemaGroupType(Schema<?> schema) {
        String schemaType = schema.getType();
        Set<String> schemaTypes = schema.getTypes();
        if (schemaType == null && schemaTypes != null) {
            if (schemaTypes.size() == 1) {
                schemaType = schemaTypes.iterator().next();
            } else if (schemaTypes.size() > 1) {
                return SchemaGroupType.INVALID;
            }
        }

        if ("boolean".equals(schemaType)) {
            return SchemaGroupType.BOOLEAN;
        }
        if ("integer".equals(schemaType)) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return SchemaGroupType.ENUM;
            }
            return SchemaGroupType.INTEGER;
        }
        if ("number".equals(schemaType)) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return SchemaGroupType.ENUM;
            }
            return SchemaGroupType.NUMBER;
        }
        if ("string".equals(schemaType)) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return SchemaGroupType.ENUM;
            }
            return SchemaGroupType.STRING;
        }

        if ("array".equals(schemaType)) {
            return SchemaGroupType.ARRAY;
        }

        if (schemaType == null || schemaType.isEmpty() || "object".equals(schemaType)) {
            // If ref
            final var ref = schema.get$ref();
            final var allOf = schema.getAllOf();
            final var oneOf = schema.getOneOf();
            final var anyOf = schema.getAnyOf();
            final var additionalProperties = schema.getAdditionalProperties();

            if (ref != null && allOf == null && oneOf == null && anyOf == null && additionalProperties == null) {
                return SchemaGroupType.REF;
            }
            if (ref == null && allOf != null && oneOf == null && anyOf == null && additionalProperties == null) {
                if (allOf.isEmpty()) {
                    return SchemaGroupType.ALL_OF_EMPTY;
                } else if (allOf.size() == 1) {
                    return SchemaGroupType.ALL_OF_SINGLE;
                } else {
                    return SchemaGroupType.ALL_OF_MULTI;
                }
            }
            if (ref == null && allOf == null && oneOf != null && anyOf == null && additionalProperties == null) {
                if (oneOf.isEmpty()) {
                    return SchemaGroupType.ONE_OF_EMPTY;
                } else if (oneOf.size() == 1) {
                    return SchemaGroupType.ONE_OF_SINGLE;
                } else {
                    return SchemaGroupType.ONE_OF_MULTI;
                }
            }

            if (ref == null && allOf == null && oneOf == null && anyOf != null && additionalProperties == null) {
                if (anyOf.isEmpty()) {
                    return SchemaGroupType.ANY_OF_EMPTY;
                } else if (anyOf.size() == 1) {
                    return SchemaGroupType.ANY_OF_SINGLE;
                } else {
                    return SchemaGroupType.ANY_OF_MULTI;
                }
            }

            // None
            if (!"object".equals(schemaType) && ref == null && allOf == null && oneOf == null && anyOf == null && additionalProperties == null) {
                return SchemaGroupType.EMPTY;
            }

            if ("object".equals(schemaType)) {
                return SchemaGroupType.OBJECT;
            }

            // Handle oneof, any of ect.
            throw new IllegalStateException("Unsupported schema (a): " + schema);
        }

        throw new IllegalStateException("Unsupported schema (b): " + schema);

    }
}
