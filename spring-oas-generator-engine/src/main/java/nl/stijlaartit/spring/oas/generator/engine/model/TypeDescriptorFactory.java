package nl.stijlaartit.spring.oas.generator.engine.model;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaUtil;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ListSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.MapSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class TypeDescriptorFactory {

    private final SchemaTypes schemaTypes;

    public TypeDescriptorFactory(SchemaTypes schemaTypes) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
    }


    public TypeDescriptor build(Schema<?> schema) {
        final var concreteSchema = schemaTypes.resolveConcrete(schema);

        switch (concreteSchema) {
            case GeneratedSchemaType generated -> {
                return TypeDescriptor.complex(generated.name());
            }
            case ListSchemaType listType -> {
                TypeDescriptor elementType = build(listType.itemInstance().schema());
                return TypeDescriptor.list(elementType);
            }
            case MapSchemaType mapType -> {
                TypeDescriptor valueType = build(mapType.valueInstance().schema());
                return TypeDescriptor.map(valueType);
            }
            case JavaSchemaType ignored -> {
                return resolvePrimitiveType(concreteSchema.schema());
            }
            case null, default -> {
            }
        }

        return resolvePrimitiveType(concreteSchema.schema());
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

    private TypeDescriptor mapStringType(@Nullable String format) {
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
