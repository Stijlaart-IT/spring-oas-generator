package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBinarySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBooleanSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleLongSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleNumberSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.schematype.CompositeSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ListSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;

import java.util.Objects;
import java.util.Optional;

public final class TypeDescriptorFactory {

    private final SchemaTypes schemaTypes;
    private final String modelsPackage;

    public TypeDescriptorFactory(SchemaTypes schemaTypes, String modelsPackage) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
        this.modelsPackage = Objects.requireNonNull(modelsPackage);
    }


    public TypeDescriptor build(SimpleSchema schema) {
        final var schemaType = schemaTypes.resolveConcrete(schema);
        if (schemaType instanceof CompositeSchemaType compositeSchemaType) {
            CompositeObjectPropertiesHelper compositeObjectPropertiesHelper = new CompositeObjectPropertiesHelper(schemaTypes);
            CompositeObjectPropertiesHelper.Result result = compositeObjectPropertiesHelper.collectCompositeObjectProperties(compositeSchemaType);
            if (result instanceof CompositeObjectPropertiesHelper.Result.Mixed) {
                return TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"));
            }
        }

        final var concreteSchema = schemaTypes.resolveConcrete(schema);

        return switch (concreteSchema) {
            case GeneratedSchemaType generated -> TypeDescriptor.qualified(modelsPackage, generated.name());
            case ListSchemaType listType -> {
                TypeDescriptor elementType = build(listType.itemInstance().schema());
                yield TypeDescriptor.list(elementType);
            }
            case JavaSchemaType ignored -> resolvePrimitiveType(concreteSchema.schema());
        };
    }

    private TypeDescriptor resolvePrimitiveType(SimpleSchema schema) {
        return switch (schema) {
            case SimpleStringSchema ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"));
            case StringEnumSchema ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"));
            case SimpleIntegerSchema ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer"));
            case SimpleLongSchema ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long"));
            case IntegerEnumSchema ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer"));
            case SimpleNumberSchema ignored -> TypeDescriptor.qualified("java.math", new JavaTypeName.Reserved("BigDecimal"));
            case NumberEnumSchema ignored -> TypeDescriptor.qualified("java.math", new JavaTypeName.Reserved("BigDecimal"));
            case SimpleBinarySchema ignored -> TypeDescriptor.qualified("org.springframework.core.io", new JavaTypeName.Generated("Resource"));
            case SimpleBooleanSchema ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Boolean"));
            default -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"));
        };
    }
}
