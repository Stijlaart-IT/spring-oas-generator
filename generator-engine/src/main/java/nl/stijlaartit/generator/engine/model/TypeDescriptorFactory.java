package nl.stijlaartit.generator.engine.model;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.generator.engine.naming.NamingUtil;
import nl.stijlaartit.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.generator.engine.schemas.SchemaParent;
import nl.stijlaartit.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.generator.engine.schemas.SchemaUtil;
import nl.stijlaartit.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.generator.engine.schematype.ListSchemaType;
import nl.stijlaartit.generator.engine.schematype.MapSchemaType;
import nl.stijlaartit.generator.engine.schematype.RefSchemaType;
import nl.stijlaartit.generator.engine.schematype.SchemaType;
import nl.stijlaartit.generator.engine.schematype.SchemaTypes;

import java.util.Objects;

public final class TypeDescriptorFactory {

    private final SchemaTypes schemaTypes;
    private final SchemaRegistry registry;

    public TypeDescriptorFactory(SchemaTypes schemaTypes, SchemaRegistry registry) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
        this.registry = Objects.requireNonNull(registry);
    }

    public TypeDescriptor build(Schema<?> schema) {
        if (schema == null) {
            return TypeDescriptor.simple("java.lang.Object");
        }

        if (schema.get$ref() != null && !schema.get$ref().isBlank()) {
            String refName = extractRefName(schema.get$ref());
            Schema<?> resolved = resolveComponentSchema(refName);
            if (resolved != null) {
                return build(resolved);
            }
            return TypeDescriptor.complex(NamingUtil.toPascalCase(refName));
        }

        SchemaType schemaType = resolveSchemaType(schema);
        if (schemaType instanceof GeneratedSchemaType generated) {
            return TypeDescriptor.complex(generated.name());
        }
        if (schemaType instanceof ListSchemaType listType) {
            TypeDescriptor elementType = build(listType.itemInstance().getSchema());
            return TypeDescriptor.list(elementType);
        }
        if (schemaType instanceof MapSchemaType mapType) {
            TypeDescriptor valueType = build(mapType.valueInstance().getSchema());
            return TypeDescriptor.map(valueType);
        }
        if (schemaType instanceof RefSchemaType refType) {
            String refName = extractRefName(refType.ref());
            Schema<?> resolved = resolveComponentSchema(refName);
            if (resolved != null) {
                return build(resolved);
            }
            return TypeDescriptor.complex(NamingUtil.toPascalCase(refName));
        }
        if (schemaType instanceof JavaSchemaType) {
            return resolvePrimitiveType(schema);
        }

        return resolvePrimitiveType(schema);
    }

    private SchemaType resolveSchemaType(Schema<?> schema) {
        try {
            return schemaTypes.resolveFromSchema(schema);
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    private Schema<?> resolveComponentSchema(String refName) {
        for (SchemaInstance instance : registry.getInstances()) {
            if (instance.getParent() instanceof SchemaParent.ComponentParent parent) {
                if (parent.componentName().equals(refName)) {
                    return instance.getSchema();
                }
            }
        }
        return null;
    }

    private String extractRefName(String ref) {
        int lastSlash = ref.lastIndexOf('/');
        return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
    }

    private TypeDescriptor resolvePrimitiveType(Schema<?> schema) {
        String type = SchemaUtil.schemaTypeName(schema);
        String format = schema.getFormat();
        return switch (type != null ? type : "") {
            case "string" -> mapStringType(format);
            case "integer" -> switch (format != null ? format : "") {
                case "int64" -> TypeDescriptor.simple("java.lang.Long");
                case "int32" -> TypeDescriptor.simple("java.lang.Integer");
                default -> TypeDescriptor.simple("java.lang.Integer");
            };
            case "number" -> switch (format != null ? format : "") {
                case "float" -> TypeDescriptor.simple("java.lang.Float");
                case "double" -> TypeDescriptor.simple("java.lang.Double");
                default -> TypeDescriptor.simple("java.math.BigDecimal");
            };
            case "boolean" -> TypeDescriptor.simple("java.lang.Boolean");
            default -> TypeDescriptor.simple("java.lang.Object");
        };
    }

    private TypeDescriptor mapStringType(String format) {
        if (format == null) {
            return TypeDescriptor.simple("java.lang.String");
        }
        return switch (format) {
            case "date" -> TypeDescriptor.simple("java.time.LocalDate");
            case "date-time" -> TypeDescriptor.simple("java.time.OffsetDateTime");
            case "uuid" -> TypeDescriptor.simple("java.util.UUID");
            default -> TypeDescriptor.simple("java.lang.String");
        };
    }
}
