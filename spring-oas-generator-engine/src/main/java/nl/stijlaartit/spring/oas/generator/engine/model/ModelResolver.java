package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.EnumModel;
import nl.stijlaartit.spring.oas.generator.domain.file.EnumValueType;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordField;
import nl.stijlaartit.spring.oas.generator.domain.file.ModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordModel;
import nl.stijlaartit.spring.oas.generator.domain.file.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaParameterName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.serialization.JavaIdentifierUtils;
import nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBooleanSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleNumberSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schematype.CompositeSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ConcreteSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.EnumSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ObjectSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.spring.oas.generator.engine.schematype.UnionSchemaType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ModelResolver {

    private final SchemaTypes schemaTypes;
    private final TypeDescriptorFactory typeDescriptorFactory;
    private final Logger logger;

    public ModelResolver(SchemaTypes schemaTypes, TypeDescriptorFactory typeDescriptorFactory, Logger logger) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
        this.typeDescriptorFactory = typeDescriptorFactory;
        this.logger = logger;
    }

    public List<ModelFile> resolve() {
        return schemaTypes.generatedSchemaTypes()
                .stream()
                .flatMap(schemType -> createModelFile(schemType).stream())
                .toList();
    }

    private Optional<ModelFile> createModelFile(GeneratedSchemaType generatedSchemaType) {
        return switch (generatedSchemaType) {
            case ObjectSchemaType n -> Optional.of(createModelFileForObjectSchemaType(n));
            case EnumSchemaType enumSchemaType -> Optional.of(createModelFileForEnumSchemaType(enumSchemaType));
            case UnionSchemaType unionSchemaType -> Optional.of(createModelFileForUnionSchemaType(unionSchemaType));
            case CompositeSchemaType compositeSchemaType -> createModelFileForCompositeSchemaType(compositeSchemaType);
        };
    }

    private RecordModel createModelFileForObjectSchemaType(ObjectSchemaType objectSchemaType) {
        final SimpleObjectSchema objectSchema = objectSchemaType.objectSchema();

        final var fields = objectSchema.properties()
                .stream()
                .map(property -> {
                    String jsonName = property.propertyName();
                    final var javaName = new JavaParameterName(JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(jsonName)));
                    TypeDescriptor type = typeDescriptorFactory.build(property.schema());
                    boolean nullable = property.schema().isNullable();
                    boolean isRequired = objectSchema.requiredProperties().contains(jsonName);
                    return new RecordField(javaName, jsonName, type, isRequired, nullable, false);
                })
                .toList();

        return new RecordModel(objectSchemaType.name(), fields, objectSchema.additionalProperties().isPresent());
    }

    private ModelFile createModelFileForEnumSchemaType(EnumSchemaType enumSchemaType) {
        final var instances = enumSchemaType.instances();
        if (instances.isEmpty()) {
            throw new IllegalStateException("Enum schema " + enumSchemaType.name() + " has no instances.");
        }
        final SimpleSchema schema = instances.getFirst().schema();

        List<String> values = resolveEnumValues(schema);
        EnumValueType valueType = resolveEnumValueType(schema);
        return new EnumModel(enumSchemaType.name(), valueType, values);
    }

    private ModelFile createModelFileForUnionSchemaType(UnionSchemaType unionSchemaType) {
        String discriminator = unionSchemaType.discriminatorProperty();
        List<OneOfVariant> variants = new ArrayList<>();
        List<SchemaInstance> variantInstances = unionSchemaType.variantInstances();
        boolean unresolvedDiscriminatorValue = false;
        for (int i = 0; i < variantInstances.size(); i++) {
            SchemaInstance variantInstance = variantInstances.get(i);
            SimpleSchema variantSchema = variantInstance.schema();
            ConcreteSchemaType concreteSchemaType = schemaTypes.resolveConcrete(variantSchema);
            String discriminatorValue = null;
            if (discriminatorValue == null && discriminator != null && !discriminator.isBlank()) {
                discriminatorValue = inferDiscriminatorValue(variantSchema, concreteSchemaType, discriminator);
            }
            if (discriminator != null && !discriminator.isBlank() && (discriminatorValue == null || discriminatorValue.isBlank())) {
                unresolvedDiscriminatorValue = true;
            }
            variants.add(new OneOfVariant(concreteSchemaType.name(), discriminatorValue));
        }

        if (unresolvedDiscriminatorValue) {
            discriminator = null;
            variants = variants.stream()
                    .map(variant -> new OneOfVariant(variant.modelName(), null))
                    .toList();
        }

        return new UnionModelFile(unionSchemaType.name(), variants, discriminator);
    }

    private Optional<ModelFile> createModelFileForCompositeSchemaType(CompositeSchemaType compositeSchemaType) {
        CompositeObjectPropertiesHelper compositeObjectPropertiesHelper = new CompositeObjectPropertiesHelper(schemaTypes);
        CompositeObjectPropertiesHelper.Result result = compositeObjectPropertiesHelper.collectCompositeObjectProperties(compositeSchemaType);
        return switch (result) {
            case CompositeObjectPropertiesHelper.Result.Mixed mixed -> {
                logger.warn("Found composite object with different mixed-type schemas with name [" + compositeSchemaType.name().value() + "]. Not generating a model for this schema.");
                yield Optional.empty();
            }

            case CompositeObjectPropertiesHelper.Result.Props props -> {
                final var fields = props.properties()
                        .entrySet()
                        .stream()
                        .map(property -> {
                            String jsonName = property.getKey();
                            final var objectProperty = property.getValue();
                            final var javaName = new JavaParameterName(JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(jsonName)));
                            TypeDescriptor type = typeDescriptorFactory.build(objectProperty.schema());
                            boolean nullable = objectProperty.schema().isNullable();
                            boolean isRequired = props.requiredProperties().contains(jsonName);
                            return new RecordField(javaName, jsonName, type, isRequired, nullable, false);
                        })
                        .toList();

                yield Optional.of(new RecordModel(compositeSchemaType.name(), fields, false));
            }
        };
    }

    private EnumValueType resolveEnumValueType(SimpleSchema schema) {
        return switch (schema) {
            case SimpleIntegerSchema ignored -> EnumValueType.INTEGER;
            case IntegerEnumSchema ignored -> EnumValueType.INTEGER;
            case SimpleNumberSchema ignored -> EnumValueType.NUMBER;
            case NumberEnumSchema ignored -> EnumValueType.NUMBER;
            case SimpleBooleanSchema ignored -> EnumValueType.BOOLEAN;
            case SimpleStringSchema ignored -> EnumValueType.STRING;
            case StringEnumSchema ignored -> EnumValueType.STRING;
            default -> EnumValueType.STRING;
        };
    }

    private List<String> resolveEnumValues(SimpleSchema schema) {
        return switch (schema) {
            case StringEnumSchema enumSchema -> enumSchema.enumValues();
            case IntegerEnumSchema enumSchema -> enumSchema.enumValues().stream().map(String::valueOf).toList();
            case NumberEnumSchema enumSchema -> enumSchema.enumValues().stream().map(String::valueOf).toList();
            default -> List.of();
        };
    }

    private String inferDiscriminatorValue(SimpleSchema variantSchema, ConcreteSchemaType concreteSchemaType, @Nullable String discriminatorProperty) {
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
            CompositeObjectPropertiesHelper compositeObjectPropertiesHelper = new CompositeObjectPropertiesHelper(schemaTypes);
            CompositeObjectPropertiesHelper.Result result = compositeObjectPropertiesHelper.collectCompositeObjectProperties(compositeSchemaType);
            if (result instanceof CompositeObjectPropertiesHelper.Result.Props props) {
                final var property = props.properties().get(discriminatorProperty);
                if (property != null) {
                    return switch (property.schema()) {
                        case StringEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                                enumSchema.enumValues().getFirst();
                        case IntegerEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                                String.valueOf(enumSchema.enumValues().getFirst());
                        case NumberEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                                String.valueOf(enumSchema.enumValues().getFirst());
                        default -> null;
                    };
                }
            }
        }
        return null;
    }

    private String inferDiscriminatorValueFromSchema(SimpleObjectSchema objectSchema, @Nullable String discriminatorProperty) {
        if (objectSchema == null) {
            return null;
        }
        return objectSchema.properties().stream()
                .filter(property -> property.propertyName().equals(discriminatorProperty))
                .findFirst()
                .map(property -> switch (property.schema()) {
                    case StringEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                            enumSchema.enumValues().getFirst();
                    case IntegerEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                            String.valueOf(enumSchema.enumValues().getFirst());
                    case NumberEnumSchema enumSchema when enumSchema.enumValues().size() == 1 ->
                            String.valueOf(enumSchema.enumValues().getFirst());
                    default -> null;
                })
                .orElse(null);
    }
}
