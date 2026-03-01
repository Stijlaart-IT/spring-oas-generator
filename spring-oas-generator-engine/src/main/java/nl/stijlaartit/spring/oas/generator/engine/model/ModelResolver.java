package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.EnumModel;
import nl.stijlaartit.spring.oas.generator.domain.file.EnumValueType;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordModel;
import nl.stijlaartit.spring.oas.generator.domain.file.ModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordField;
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
import nl.stijlaartit.spring.oas.generator.engine.schematype.CompositeSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.EnumSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ObjectSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.spring.oas.generator.engine.schematype.UnionSchemaType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ModelResolver {

    private final SchemaTypes schemaTypes;
    private final TypeInfoResolver typeInfoResolver;
    private final Logger logger;

    public ModelResolver(SchemaTypes schemaTypes, TypeInfoResolver typeInfoResolver, Logger logger) {
        this.schemaTypes = Objects.requireNonNull(schemaTypes);
        this.typeInfoResolver = Objects.requireNonNull(typeInfoResolver);
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
                    final var schemaType = schemaTypes.resolveConcrete(property.schema());
                    TypeDescriptor type = typeInfoResolver.get(schemaType).typeDescriptor();

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
        TypeInfo typeInfo = typeInfoResolver.get(unionSchemaType);
        TypeInfo.UnionTypeInfo unionTypeInfo = (TypeInfo.UnionTypeInfo) typeInfo;
        List<OneOfVariant> variants = unionTypeInfo.variants().stream()
                .map(variant -> new OneOfVariant(variant.modelName(), variant.discriminatorValue()))
                .toList();
        return new UnionModelFile(unionSchemaType.name(), variants, unionTypeInfo.discriminatorProperty());
    }

    private Optional<ModelFile> createModelFileForCompositeSchemaType(CompositeSchemaType compositeSchemaType) {
        TypeInfo typeInfo = typeInfoResolver.get(compositeSchemaType);
        TypeInfo.CompositeTypeInfo compositeTypeInfo = (TypeInfo.CompositeTypeInfo) typeInfo;
        if (compositeTypeInfo.properties().isEmpty()) {
            return Optional.empty();
        }

        final var props = compositeTypeInfo.properties().orElseThrow();
        final var fields = props.properties()
                .entrySet()
                .stream()
                .map(property -> {
                    String jsonName = property.getKey();
                    final var objectProperty = property.getValue();
                    final var javaName = new JavaParameterName(JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(jsonName)));
                    TypeDescriptor type = typeInfoResolver.get(objectProperty.schema()).typeDescriptor();
                    boolean nullable = objectProperty.schema().isNullable();
                    boolean isRequired = props.requiredProperties().contains(jsonName);
                    return new RecordField(javaName, jsonName, type, isRequired, nullable, false);
                })
                .toList();

        return Optional.of(new RecordModel(compositeSchemaType.name(), fields, false));
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
}
