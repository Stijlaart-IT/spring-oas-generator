package nl.stijlaartit.spring.oas.generator.engine.model;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.domain.EnumModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.EnumValueType;
import nl.stijlaartit.spring.oas.generator.engine.domain.FieldModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.ModelFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.engine.domain.RecordModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaUtil;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ConcreteSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.EnumSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ObjectSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.spring.oas.generator.engine.schematype.UnionSchemaType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ModelResolver {

    private final SchemaTypes schemaTypes;
    private final TypeDescriptorFactory typeDescriptorFactory;

    public ModelResolver( SchemaTypes schemaTypes, TypeDescriptorFactory typeDescriptorFactory) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
        this.typeDescriptorFactory = typeDescriptorFactory;
    }

    public List<ModelFile> resolve(SchemaTypes schemaTypes) {
        return schemaTypes.generatedSchemaTypes()
                .stream()
                .map(generatedSchemaType -> createModelFile(generatedSchemaType, schemaTypes))
                .toList();
    }

    private ModelFile createModelFile(GeneratedSchemaType generatedSchemaType, SchemaTypes schemaTypes) {
        return switch (generatedSchemaType) {
            case ObjectSchemaType n ->
                    createModelFileForObjectSchemaType(n);
            case EnumSchemaType enumSchemaType ->
                    createModelFileForEnumSchemaType(enumSchemaType);
            case UnionSchemaType unionSchemaType ->
                    createModelFileForUnionSchemaType(unionSchemaType, schemaTypes);
        };
    }

    private RecordModel createModelFileForObjectSchemaType(ObjectSchemaType objectSchemaType) {
        Schema<?> schema = objectSchemaType.schema();

        boolean additionalProperties = schema.getAdditionalProperties() instanceof Schema<?>
                || Boolean.TRUE.equals(schema.getAdditionalProperties());

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

        return new RecordModel(objectSchemaType.name(), fields, additionalProperties);
    }

    private ModelFile createModelFileForEnumSchemaType(EnumSchemaType enumSchemaType) {
        final var  instances = enumSchemaType.instances();
        if (instances.isEmpty()) {
            throw new IllegalStateException("Enum schema " + enumSchemaType.name() + " has no instances.");
        }
        Schema<?> schema = instances.getFirst().schema();

        List<String> values = schema.getEnum() != null
                ? schema.getEnum().stream().map(String::valueOf).toList()
                : List.of();
        EnumValueType valueType = resolveEnumValueType(schema);
        return new EnumModel(enumSchemaType.name(), valueType, values);
    }

    private ModelFile createModelFileForUnionSchemaType(UnionSchemaType unionSchemaType,
                                                        SchemaTypes schemaTypes) {
        Schema<?> schema = unionSchemaType.instances().isEmpty() ? null : unionSchemaType.instances().getFirst().schema();

        String discriminator = resolveDiscriminatorProperty(schema);
        List<OneOfVariant> variants = new ArrayList<>();
        List<SchemaInstance> variantInstances = unionSchemaType.variantInstances();
        for (SchemaInstance variantInstance : variantInstances) {
            Schema<?> variantSchema = variantInstance.schema();
            ConcreteSchemaType concreteSchemaType = schemaTypes.resolveConcrete(variantSchema);
            String discriminatorValue = resolveDiscriminatorValue(variantSchema, discriminator);
            variants.add(new OneOfVariant(concreteSchemaType.name(), discriminatorValue));
        }


        return new UnionModelFile(unionSchemaType.name(), variants, discriminator);
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

        ConcreteSchemaType resolvedVariant = schemaTypes.resolveConcrete(variant);
        Map<String, Schema> properties = collectProperties(resolvedVariant);
        Schema<?> discriminatorSchema = properties.get(discriminatorProperty);
        if (discriminatorSchema == null) {
            return null;
        }

        ConcreteSchemaType concreteSchemaType = schemaTypes.resolveConcrete(discriminatorSchema);
        SchemaInstance schemaInstance = concreteSchemaType.instances().getFirst();
        final var resolvedProperty = schemaInstance.schema();
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
            final var resolvedConcrete = schemaTypes.resolveConcrete(variant);
            Map<String, Schema> properties = collectProperties(resolvedConcrete);
            Set<String> localCandidates = new LinkedHashSet<>();
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                final var concreteProperty = schemaTypes.resolveConcrete(entry.getValue());
                Schema<?> property = concreteProperty.instances().getFirst().schema();
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

    private Map<String, Schema> collectProperties(ConcreteSchemaType concreteSchemaType) {
        return switch (concreteSchemaType) {
            case GeneratedSchemaType generatedSchemaType -> switch (generatedSchemaType) {
                case ObjectSchemaType objectSchemaType -> collectProperties(objectSchemaType.schema());
                case EnumSchemaType enumSchemaType -> collectProperties(enumSchemaType.schema());
                case UnionSchemaType unionSchemaType -> collectProperties(unionSchemaType.schema());
            };
            case JavaSchemaType ignored -> Map.of();
        };
    }

    private Map<String, Schema> collectProperties(Schema<?> schema) {
        Map<String, Schema> properties = new LinkedHashMap<>();
        if (schema.getAllOf() != null) {
            for (Schema<?> part : schema.getAllOf()) {
                final var partSchemaType = schemaTypes.resolveConcrete(part);
                properties.putAll(collectProperties(partSchemaType));
            }
        }
        if (schema.getProperties() != null) {
            properties.putAll(schema.getProperties());
        }
        return properties;
    }

    private Set<String> collectRequired(ConcreteSchemaType concreteSchemaType) {
        return switch (concreteSchemaType) {
            case GeneratedSchemaType generatedSchemaType -> switch (generatedSchemaType) {
                case ObjectSchemaType objectSchemaType -> collectRequired(objectSchemaType.schema());
                case EnumSchemaType enumSchemaType -> collectRequired(enumSchemaType.schema());
                case UnionSchemaType unionSchemaType -> collectRequired(unionSchemaType.schema());
            };
            case JavaSchemaType ignored -> Set.of();
        };
    }

    private Set<String> collectRequired(Schema<?> schema) {
        Set<String> required = new LinkedHashSet<>();
        if (schema.getAllOf() != null) {
            for (Schema<?> part : schema.getAllOf()) {
                final var partSchemaType = schemaTypes.resolveConcrete(part);
                required.addAll(collectRequired(partSchemaType));
            }
        }
        if (schema.getRequired() != null) {
            required.addAll(schema.getRequired());
        }
        return required;
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
}
