package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.schematype.BinarySchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.BooleanSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.CompositeSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ConcreteSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.DecimalSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.EmptySchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.EnumSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.IntegerSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ListSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.LongSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ObjectSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.spring.oas.generator.engine.schematype.StringSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.UnionSchemaType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class TypeInfoResolver {

    private final SchemaTypes schemaTypes;
    private final String modelsPackage;
    private final Map<SchemaType, TypeInfo> typeInfoBySchemaType = new LinkedHashMap<>();

    private TypeInfoResolver(SchemaTypes schemaTypes, String modelsPackage) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
        this.modelsPackage = Objects.requireNonNull(modelsPackage);
    }

    public static TypeInfoResolver resolve(SchemaTypes schemaTypes, String modelsPackage) {
        TypeInfoResolver resolver = new TypeInfoResolver(schemaTypes, modelsPackage);
        for (SchemaType schemaType : schemaTypes.types()) {
            resolver.resolveAndStore(schemaType);
        }
        return resolver;
    }

    public TypeInfo get(SchemaType schemaType) {
        final SchemaType nonNullSchemaType = Objects.requireNonNull(schemaType);
        TypeInfo typeInfo = typeInfoBySchemaType.get(nonNullSchemaType);
        if (typeInfo != null) {
            return typeInfo;
        }
        return resolveAndStore(nonNullSchemaType);
    }

    public TypeInfo get(SimpleSchema schema) {
        return get(schemaTypes.resolveFromSchema(schema));
    }

    private TypeInfo resolveAndStore(SchemaType schemaType) {
        TypeInfo typeInfo = typeInfoBySchemaType.get(schemaType);
        if (typeInfo != null) {
            return typeInfo;
        }

        ConcreteSchemaType concreteSchemaType = schemaTypes.resolveConcrete(schemaType);
        TypeInfo concreteTypeInfo = typeInfoBySchemaType.get(concreteSchemaType);
        if (concreteTypeInfo == null) {
            concreteTypeInfo = createTypeInfo(concreteSchemaType);
            typeInfoBySchemaType.put(concreteSchemaType, concreteTypeInfo);
        }
        typeInfoBySchemaType.put(schemaType, concreteTypeInfo);
        return concreteTypeInfo;
    }

    private TypeInfo createTypeInfo(ConcreteSchemaType concreteSchemaType) {
        return switch (concreteSchemaType) {
            case CompositeSchemaType compositeSchemaType -> createCompositeTypeInfo(compositeSchemaType);
            case UnionSchemaType unionSchemaType -> createUnionTypeInfo(unionSchemaType);
            default -> new TypeInfo.BasicTypeInfo(resolveTypeDescriptor(concreteSchemaType));
        };
    }

    private TypeInfo.CompositeTypeInfo createCompositeTypeInfo(CompositeSchemaType compositeSchemaType) {
        final Set<ConcreteSchemaType> componentSchemaTypes = compositeSchemaType.compositeSchema().components().stream()
                .map(schemaTypes::resolveConcrete)
                .collect(Collectors.toSet());

        if (componentSchemaTypes.size() == 1) {
            TypeDescriptor typeDescriptor = resolveTypeDescriptor(componentSchemaTypes.iterator().next());
            return new TypeInfo.CompositeTypeInfo(typeDescriptor, Optional.empty());
        }

        Result result = collectCompositeObjectProperties(compositeSchemaType);
        return switch (result) {
            case Result.Mixed ignored -> new TypeInfo.CompositeTypeInfo(
                    TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object")),
                    Optional.empty()
            );
            case Result.Props props -> new TypeInfo.CompositeTypeInfo(
                    resolveTypeDescriptor(compositeSchemaType),
                    Optional.of(new TypeInfo.CompositeProperties(props.properties(), props.requiredProperties()))
            );
        };
    }

    private TypeInfo.UnionTypeInfo createUnionTypeInfo(UnionSchemaType unionSchemaType) {
        String discriminator = unionSchemaType.discriminatorProperty();
        List<TypeInfo.UnionVariantInfo> variants = new ArrayList<>();
        boolean unresolvedDiscriminatorValue = false;

        for (var variantInstance : unionSchemaType.variantInstances()) {
            SimpleSchema variantSchema = variantInstance.schema();
            ConcreteSchemaType concreteSchemaType = schemaTypes.resolveConcrete(variantSchema);

            String discriminatorValue = null;
            if (discriminator != null && !discriminator.isBlank()) {
                discriminatorValue = inferDiscriminatorValue(variantSchema, concreteSchemaType, discriminator);
            }
            if (discriminator != null && !discriminator.isBlank() && (discriminatorValue == null || discriminatorValue.isBlank())) {
                unresolvedDiscriminatorValue = true;
            }

            variants.add(new TypeInfo.UnionVariantInfo(concreteSchemaType.name(), discriminatorValue));
        }

        if (unresolvedDiscriminatorValue) {
            discriminator = null;
            variants = variants.stream()
                    .map(v -> new TypeInfo.UnionVariantInfo(v.modelName(), null))
                    .toList();
        }

        return new TypeInfo.UnionTypeInfo(
                resolveTypeDescriptor(unionSchemaType),
                discriminator,
                variants
        );
    }

    private TypeDescriptor resolveTypeDescriptor(ConcreteSchemaType concreteSchemaType) {
        return switch (concreteSchemaType) {
            case GeneratedSchemaType generatedSchemaType ->
                    TypeDescriptor.qualified(modelsPackage, generatedSchemaType.name());
            case ListSchemaType listSchemaType -> TypeDescriptor.list(get(listSchemaType.itemInstance().schema()).typeDescriptor());
            case StringSchemaType ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"));
            case IntegerSchemaType ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer"));
            case LongSchemaType ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Long"));
            case BooleanSchemaType ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Boolean"));
            case DecimalSchemaType ignored -> TypeDescriptor.qualified("java.math", new JavaTypeName.Reserved("BigDecimal"));
            case BinarySchemaType ignored ->
                    TypeDescriptor.qualified("org.springframework.core.io", new JavaTypeName.Generated("Resource"));
            case EmptySchemaType ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"));
            case JavaSchemaType ignored -> TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"));
        };
    }

    private Result collectCompositeObjectProperties(ConcreteSchemaType concreteSchemaType) {
        return switch (concreteSchemaType) {
            case GeneratedSchemaType generatedSchemaType -> switch (generatedSchemaType) {
                case ObjectSchemaType objectSchemaType -> {
                    final Map<String, ObjectProperty> props = new LinkedHashMap<>();
                    for (ObjectProperty property : objectSchemaType.objectSchema().properties()) {
                        props.put(property.propertyName(), property);
                    }
                    yield new Result.Props(props, new LinkedHashSet<>(objectSchemaType.objectSchema().requiredProperties()));
                }
                case EnumSchemaType enumSchemaType -> new Result.Mixed(enumSchemaType.name(), "Enum not supported in composite types");
                case UnionSchemaType unionSchemaType -> new Result.Mixed(unionSchemaType.name(), "Union not supported in composite types");
                case CompositeSchemaType compositeSchemaType -> collectNestedProperties(compositeSchemaType.compositeSchema());
            };
            case JavaSchemaType javaSchemaType -> new Result.Mixed(javaSchemaType.name(), "Primitives not supported");
        };
    }

    private Result collectNestedProperties(CompositeSchema compositeSchema) {
        List<SimpleSchema> allOf = Objects.requireNonNull(compositeSchema.components());
        Map<String, ObjectProperty> properties = new LinkedHashMap<>();
        Set<String> requiredProperties = new LinkedHashSet<>();
        for (SimpleSchema part : allOf) {
            final ConcreteSchemaType partSchemaType = schemaTypes.resolveConcrete(part);
            Result collectProperties = collectCompositeObjectProperties(partSchemaType);
            switch (collectProperties) {
                case Result.Mixed mixed -> {
                    return mixed;
                }
                case Result.Props props -> {
                    properties.putAll(props.properties());
                    requiredProperties.addAll(props.requiredProperties());
                }
            }
        }
        return new Result.Props(properties, requiredProperties);
    }

    private @Nullable String inferDiscriminatorValue(SimpleSchema variantSchema,
                                                     ConcreteSchemaType concreteSchemaType,
                                                     @Nullable String discriminatorProperty) {
        SimpleObjectSchema objectSchema = null;
        if (variantSchema instanceof SimpleObjectSchema simpleObjectSchema) {
            objectSchema = simpleObjectSchema;
        } else if (concreteSchemaType instanceof ObjectSchemaType objectSchemaType) {
            objectSchema = objectSchemaType.objectSchema();
        }
        if (objectSchema != null) {
            return inferDiscriminatorValueFromSchema(objectSchema, discriminatorProperty);
        }

        if (concreteSchemaType instanceof CompositeSchemaType compositeSchemaType) {
            TypeInfo typeInfo = get(compositeSchemaType);
            if (typeInfo instanceof TypeInfo.CompositeTypeInfo compositeTypeInfo && compositeTypeInfo.properties().isPresent()) {
                final var property = compositeTypeInfo.properties().get().properties().get(discriminatorProperty);
                if (property != null) {
                    return singleValueEnumLiteral(property.schema());
                }
            }
        }
        return null;
    }

    private @Nullable String inferDiscriminatorValueFromSchema(SimpleObjectSchema objectSchema,
                                                               @Nullable String discriminatorProperty) {
        if (objectSchema == null || discriminatorProperty == null || discriminatorProperty.isBlank()) {
            return null;
        }
        return objectSchema.properties().stream()
                .filter(property -> property.propertyName().equals(discriminatorProperty))
                .findFirst()
                .map(property -> singleValueEnumLiteral(property.schema()))
                .orElse(null);
    }

    private @Nullable String singleValueEnumLiteral(SimpleSchema schema) {
        return switch (schema) {
            case StringEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                    enumSchema.enumValues().getFirst();
            case IntegerEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                    String.valueOf(enumSchema.enumValues().getFirst());
            case NumberEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                    String.valueOf(enumSchema.enumValues().getFirst());
            default -> null;
        };
    }

    private sealed interface Result permits Result.Mixed, Result.Props {
        record Mixed(JavaTypeName javaTypeName, String error) implements Result {
        }

        record Props(Map<String, ObjectProperty> properties, Set<String> requiredProperties) implements Result {
            public Props {
                Objects.requireNonNull(properties);
                Objects.requireNonNull(requiredProperties);
            }
        }
    }
}
