package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.RefSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleAnySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBinarySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBooleanSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleLongSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleNumberSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.naming.NameProvider;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class SchemaTypeResolver {

    private final Logger logger;
    private final SchemaRegistry registry;
    private final NameProvider nameProvider;

    public SchemaTypeResolver(SchemaRegistry registry, NameProvider nameProvider, Logger logger) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.nameProvider = Objects.requireNonNull(nameProvider, "nameProvider");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public SchemaTypes resolve() {
        SchemaInstanceGroups groups = SchemaInstanceGroup.groupBySchemaEquals(registry.getInstances());
        List<SchemaType> types = new ArrayList<>();

        for (SchemaInstanceGroup group : groups.groups()) {
            SchemaType type = createType(group);
            types.add(type);
        }

        final var names =
                types.stream()
                        .filter(type -> type instanceof GeneratedSchemaType)
                        .map(type -> (GeneratedSchemaType) type)
                        .map(GeneratedSchemaType::name)
                        .toList();

        final var uniqueNames = new HashSet<>(names);
        if (uniqueNames.size() != names.size()) {
            throw new IllegalStateException("Duplicate generated schema names: " + names);
        }
        return new SchemaTypes(types);
    }

    private SchemaType createType(SchemaInstanceGroup group) {
        return switch (group.schema()) {
            case SimpleAnySchema ignored -> new EmptySchemaType(group.instances());
            case SimpleBooleanSchema ignored -> new BooleanSchemaType(group.instances());
            case SimpleIntegerSchema ignored -> new IntegerSchemaType(group.instances());
            case SimpleLongSchema ignored -> new LongSchemaType(group.instances());
            case IntegerEnumSchema ignored -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                yield new EnumSchemaType(group.instances(), name);
            }
            case SimpleNumberSchema ignored -> new DecimalSchemaType(group.instances());
            case NumberEnumSchema ignored -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                yield new EnumSchemaType(group.instances(), name);
            }
            case SimpleStringSchema ignored -> new StringSchemaType(group.instances());
            case StringEnumSchema ignored -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                yield new EnumSchemaType(group.instances(), name);
            }
            case SimpleBinarySchema ignored -> new BinarySchemaType(group.instances());
            case SimpleArraySchema ignored -> {
                SchemaInstance itemInstance = resolveItemInstance(group.schema());
                yield new ListSchemaType(group.instances(), itemInstance);
            }
            case SimpleObjectSchema simpleObjectSchema -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                yield new ObjectSchemaType(group.instances(), name, simpleObjectSchema);
            }
            case RefSchema ignored -> {
                SchemaRef ref = resolveRef(group.schema());
                yield new RefSchemaType(group.instances(), ref);
            }
            case CompositeSchema  compositeSchema -> {
                if (compositeSchema.components().isEmpty()) {
                    logger.warn("AllOf schema without items");
                    yield new EmptySchemaType(group.instances());
                } else if (compositeSchema.components().size() == 1) {
                    final var single = ((CompositeSchema) group.schema()).components().getFirst();
                    yield new DeferredSchemaType(group.instances(), single);
                } else {
                    JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                    yield new CompositeSchemaType(name, group.instances());
                }
            }
            case UnionSchema unionSchema -> {
                if (unionSchema.variants().isEmpty()) {
                    logger.warn("OneOf schema without items");
                    yield new EmptySchemaType(group.instances());
                } else if (unionSchema.variants().size() == 1) {
                    final var single = ((UnionSchema) group.schema()).variants().getFirst();
                    yield new DeferredSchemaType(group.instances(), single);
                } else {
                    JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                    List<SchemaInstance> variants = resolveVariantInstances(group.schema());
                    yield  new UnionSchemaType(group.instances(), name, variants, unionSchema.discriminatorProperty());
                }
            }
        };
    }

    private SchemaInstance resolveItemInstance(SimpleSchema schema) {
        if (!(schema instanceof SimpleArraySchema arraySchema)) {
            throw new IllegalStateException("Array group is not backed by SimpleArraySchema.");
        }
        return registry.instanceForSchema(arraySchema.itemSchema());
    }

    private List<SchemaInstance> resolveVariantInstances(SimpleSchema schema) {
        List<SchemaInstance> variants = new ArrayList<>();
        if (schema instanceof UnionSchema unionSchema) {
            for (SimpleSchema oneOfSchema : unionSchema.variants()) {
                variants.add(registry.instanceForSchema(oneOfSchema));
            }
        }
        if (schema instanceof CompositeSchema compositeSchema) {
            for (SimpleSchema anyOfSchema : compositeSchema.components()) {
                variants.add(registry.instanceForSchema(anyOfSchema));
            }
        }
        return variants;
    }

    private SchemaRef resolveRef(SimpleSchema schema) {
        if (!(schema instanceof RefSchema refSchema)) {
            throw new IllegalStateException("Ref group is not backed by RefSchema.");
        }
        return refSchema.ref();
    }
}
