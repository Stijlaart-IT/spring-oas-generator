package nl.stijlaartit.generator.engine.model;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.generator.engine.domain.EnumModel;
import nl.stijlaartit.generator.engine.domain.EnumValueType;
import nl.stijlaartit.generator.engine.domain.FieldModel;
import nl.stijlaartit.generator.engine.domain.ModelFile;
import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.RecordModel;
import nl.stijlaartit.generator.engine.domain.UnionModelFile;
import nl.stijlaartit.generator.engine.naming.NamingUtil;
import nl.stijlaartit.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.generator.engine.schemas.SchemaParent;
import nl.stijlaartit.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.generator.engine.schemas.SchemaUtil;
import nl.stijlaartit.generator.engine.schematype.EnumSchemaType;
import nl.stijlaartit.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.generator.engine.schematype.ObjectSchemaType;
import nl.stijlaartit.generator.engine.schematype.RefSchemaType;
import nl.stijlaartit.generator.engine.schematype.SchemaType;
import nl.stijlaartit.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.generator.engine.schematype.UnionSchemaType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ModelResolver {

    private final SchemaRegistry registry;

    public ModelResolver(SchemaRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    public List<ModelFile> resolve(SchemaTypes schemaTypes2) {
        final var typeDescriptorFactory = new TypeDescriptorFactory(schemaTypes2, registry);

        final var typesToGenerate = schemaTypes2.generated();
        return typesToGenerate
                .stream()
                .map(generatedSchemaType -> createModelFile(generatedSchemaType, typeDescriptorFactory, schemaTypes2))
                .toList();
    }

    private ModelFile createModelFile(GeneratedSchemaType generatedSchemaType, TypeDescriptorFactory typeDescriptorFactory, SchemaTypes schemaTypes2) {
        return switch (generatedSchemaType) {
            case ObjectSchemaType n ->
                    createModelFileForObjectSchemaType(n, typeDescriptorFactory);
            case EnumSchemaType enumSchemaType ->
                    createModelFileForEnumSchemaType(enumSchemaType);
            case UnionSchemaType unionSchemaType ->
                    createModelFileForUnionSchemaType(unionSchemaType, schemaTypes2);
        };
    }

    private RecordModel createModelFileForObjectSchemaType(ObjectSchemaType objectSchemaType,
                                                           TypeDescriptorFactory typeDescriptorFactory) {
        Schema<?> schema = objectSchemaType.schema();

        if (isAdditionalPropertiesObject(schema)) {
            TypeDescriptor valueType;
            if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
                valueType = TypeDescriptor.map(typeDescriptorFactory.build(additional));
            } else {
                valueType = TypeDescriptor.map(TypeDescriptor.simple("java.lang.Object"));
            }
            FieldModel field = new FieldModel("value", "value", valueType, true, true, false);
            return new RecordModel(objectSchemaType.name(), List.of(field));
        }

        Set<String> requiredProperties = collectRequired(schema);
        final var fields = collectProperties(schema)
                .entrySet()
                .stream()
                .map(property -> {
                    String jsonName = property.getKey();
                    String javaName = JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(jsonName));
                    TypeDescriptor type = typeDescriptorFactory.build(property.getValue());
                    boolean nullable = Boolean.TRUE.equals(property.getValue().getNullable());
                    boolean isRequired = requiredProperties.contains(jsonName);
                    return new FieldModel(javaName, jsonName, type, isRequired, nullable, false);
                })
                .toList();

        return new RecordModel(objectSchemaType.name(), fields);
    }

    private ModelFile createModelFileForEnumSchemaType(EnumSchemaType enumSchemaType) {
        Schema<?> schema = enumSchemaType.instances().isEmpty() ? null : enumSchemaType.instances().getFirst().schema();

        List<String> values = schema != null && schema.getEnum() != null
                ? schema.getEnum().stream().map(String::valueOf).toList()
                : List.of();
        EnumValueType valueType = resolveEnumValueType(schema);
        return new EnumModel(enumSchemaType.name(), values, valueType);
    }

    private ModelFile createModelFileForUnionSchemaType(UnionSchemaType unionSchemaType,
                                                        SchemaTypes schemaTypes2) {
        Schema<?> schema = unionSchemaType.instances().isEmpty() ? null : unionSchemaType.instances().getFirst().schema();

        String discriminator = resolveDiscriminatorProperty(schema);
        List<OneOfVariant> variants = new ArrayList<>();
        List<SchemaInstance> variantInstances = unionSchemaType.variantInstances();
        for (SchemaInstance variantInstance : variantInstances) {
            Schema<?> variantSchema = variantInstance.schema();
            String variantName = resolveVariantName(variantSchema, schemaTypes2);
            String discriminatorValue = resolveDiscriminatorValue(variantSchema, discriminator);
            variants.add(new OneOfVariant(variantName, discriminatorValue));
        }

        return new UnionModelFile(unionSchemaType.name(), variants, discriminator);
    }

    private String resolveVariantName(Schema<?> variantSchema,
                                      SchemaTypes schemaTypes2) {
        if (variantSchema.get$ref() != null && !variantSchema.get$ref().isBlank()) {
            return NamingUtil.toPascalCase(extractRefName(variantSchema.get$ref()));
        }

        SchemaType schemaType;
        try {
            schemaType = schemaTypes2.resolveFromSchema(variantSchema);
        } catch (IllegalStateException ex) {
            schemaType = null;
        }

        if (schemaType instanceof GeneratedSchemaType generatedType) {
            return generatedType.name();
        }

        if (schemaType instanceof RefSchemaType refType) {
            return NamingUtil.toPascalCase(extractRefName(refType.ref()));
        }

        throw new IllegalStateException("Unable to resolve variant name for schema " + variantSchema);
    }

    @Nullable
    private String resolveDiscriminatorProperty(@Nullable Schema<?> schema) {
        if (schema == null) {
            return null;
        }
        if (schema.getDiscriminator() != null) {
            String propertyName = schema.getDiscriminator().getPropertyName();
            if (propertyName != null && !propertyName.isBlank()) {
                return propertyName;
            }
        }
        List<Schema> variants = schema.getOneOf();
        return inferDiscriminatorProperty(variants);
    }

    @Nullable
    private String resolveDiscriminatorValue(Schema<?> variant, @Nullable String discriminatorProperty) {
        if (discriminatorProperty == null || discriminatorProperty.isBlank()) {
            return null;
        }
        Schema<?> resolvedVariant = resolveRefSchema(variant);
        Map<String, Schema> properties = collectProperties(resolvedVariant);
        Schema<?> discriminatorSchema = properties.get(discriminatorProperty);
        if (discriminatorSchema == null) {
            return null;
        }
        Schema<?> resolvedProperty = resolveRefSchema(discriminatorSchema);
        if (resolvedProperty.getEnum() != null && resolvedProperty.getEnum().size() == 1) {
            return String.valueOf(resolvedProperty.getEnum().getFirst());
        }
        return null;
    }

    @Nullable
    private String inferDiscriminatorProperty(@Nullable List<Schema> variants) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        Set<String> candidates = null;
        for (Schema<?> variant : variants) {
            Schema<?> resolved = resolveRefSchema(variant);
            Map<String, Schema> properties = collectProperties(resolved);
            Set<String> localCandidates = new LinkedHashSet<>();
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                Schema<?> property = resolveRefSchema(entry.getValue());
                if (property.getEnum() != null && property.getEnum().size() == 1) {
                    localCandidates.add(entry.getKey());
                }
            }
            if (candidates == null) {
                candidates = new LinkedHashSet<>(localCandidates);
            } else {
                candidates.retainAll(localCandidates);
            }
            if (candidates.isEmpty()) {
                return null;
            }
        }
        if (candidates.contains("type")) {
            return "type";
        }
        return candidates.stream().sorted().findFirst().orElse(null);
    }

    private boolean isAdditionalPropertiesObject(Schema<?> schema) {
        return (schema.getAdditionalProperties() instanceof Schema<?>
                || Boolean.TRUE.equals(schema.getAdditionalProperties()))
                && (schema.getProperties() == null || schema.getProperties().isEmpty());
    }

    private Map<String, Schema> collectProperties(Schema<?> schema) {
        Map<String, Schema> properties = new LinkedHashMap<>();
        if (schema.getAllOf() != null) {
            for (Schema<?> part : schema.getAllOf()) {
                Schema<?> resolved = resolveRefSchema(part);
                properties.putAll(collectProperties(resolved));
            }
        }
        if (schema.getProperties() != null) {
            properties.putAll(schema.getProperties());
        }
        return properties;
    }

    private Set<String> collectRequired(Schema<?> schema) {
        Set<String> required = new LinkedHashSet<>();
        if (schema.getAllOf() != null) {
            for (Schema<?> part : schema.getAllOf()) {
                Schema<?> resolved = resolveRefSchema(part);
                required.addAll(collectRequired(resolved));
            }
        }
        if (schema.getRequired() != null) {
            required.addAll(schema.getRequired());
        }
        return required;
    }

    private Schema<?> resolveRefSchema(Schema<?> schema) {
        if (schema.get$ref() == null) {
            return schema;
        }
        String refName = extractRefName(schema.get$ref());
        Schema<?> resolved = resolveComponentSchema(refName);
        return resolved != null ? resolved : schema;
    }

    @Nullable
    private Schema<?> resolveComponentSchema(String refName) {
        for (SchemaInstance instance : registry.getInstances()) {
            if (instance.parent() instanceof SchemaParent.ComponentParent(String componentName)) {
                if (componentName.equals(refName)) {
                    return instance.schema();
                }
            }
        }
        return null;
    }

    private EnumValueType resolveEnumValueType(Schema<?> schema) {
        String type = SchemaUtil.schemaTypeName(schema);
        if ("integer".equals(type)) {
            return EnumValueType.INTEGER;
        }
        if ("number".equals(type)) {
            return EnumValueType.NUMBER;
        }
        if ("boolean".equals(type)) {
            return EnumValueType.BOOLEAN;
        }
        if ("string".equals(type)) {
            return EnumValueType.STRING;
        }
        throw new IllegalStateException("Unsupported enum schema: " + type);
    }

    private String extractRefName(String ref) {
        int lastSlash = ref.lastIndexOf('/');
        return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
    }
}
