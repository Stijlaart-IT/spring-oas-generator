package nl.stijlaartit.spring.oas.generator.engine.model;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaUtil;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ListSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class TypeDescriptorFactory {

    private final SchemaTypes schemaTypes;
    private final String modelsPackage;

    public TypeDescriptorFactory(SchemaTypes schemaTypes, String modelsPackage) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
        this.modelsPackage = Objects.requireNonNull(modelsPackage);
    }


    public TypeDescriptor build(Schema<?> schema) {
        final var concreteSchema = schemaTypes.resolveConcrete(schema);

        switch (concreteSchema) {
            case GeneratedSchemaType generated -> {
                return TypeDescriptor.qualified(modelsPackage, generated.name());
            }
            case ListSchemaType listType -> {
                TypeDescriptor elementType = build(listType.itemInstance().schema());
                return TypeDescriptor.list(elementType);
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
                case "int64" -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long"));
                case "int32" -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer"));
                default -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer"));
            };
            case "number" -> switch (format != null ? format : "") {
                case "float" -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Float"));
                case "double" -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Double"));
                default -> TypeDescriptor.qualified("java.math", new JavaTypeName.Reserved("BigDecimal"));
            };
            case "boolean" -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Boolean"));
            default -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"));
        };
    }

    private TypeDescriptor mapStringType(@Nullable String format) {
        if (format == null) {
            return TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"));
        }
        return switch (format) {
            case "date" -> TypeDescriptor.qualified("java.time", new JavaTypeName.Generated("LocalDate"));
            case "date-time" -> TypeDescriptor.qualified("java.time", new JavaTypeName.Generated("OffsetDateTime"));
            case "uuid" -> TypeDescriptor.qualified("java.util", new JavaTypeName.Generated("UUID"));
            default -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"));
        };
    }
}
