package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
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
        return switch (group.groupType()) {
            case EMPTY -> new EmptySchemaType(group.instances());
            case BOOLEAN -> new BooleanSchemaType(group.instances());
            case INTEGER -> new IntegerSchemaType(group.instances());
            case NUMBER -> new DecimalSchemaType(group.instances());
            case STRING -> new StringSchemaType(group.instances());
            case ENUM -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                yield new EnumSchemaType(group.instances(), name);
            }
            case ARRAY -> {
                SchemaInstance itemInstance = resolveItemInstance(group.schema());
                yield new ListSchemaType(group.instances(), itemInstance);
            }
            case OBJECT -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                yield new ObjectSchemaType(group.instances(), name);
            }
            case REF -> new RefSchemaType(group.instances(), SchemaRef.parseFromRefValue(group.schema().get$ref()));
            case ALL_OF_EMPTY -> {
                logger.warn("AllOf schema without items");
                yield new EmptySchemaType(group.instances());
            }
            case ALL_OF_SINGLE -> {
                final var single = group.schema().getAllOf().getFirst();
                yield new DeferredSchemaType(group.instances(), single);
            }
            case ALL_OF_MULTI -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                yield new CompositeSchemaType(name, group.instances());
            }
            case ONE_OF_EMPTY -> {
                logger.warn("OneOf schema without items");
                yield new EmptySchemaType(group.instances());
            }
            case ONE_OF_SINGLE -> {
                final var single = group.schema().getOneOf().getFirst();
                yield new DeferredSchemaType(group.instances(), single);
            }
            case ONE_OF_MULTI -> {
                JavaTypeName name = nameProvider.resolveUniqueName(group.instances());
                List<SchemaInstance> variants = resolveVariantInstances(group.schema());
                yield  new UnionSchemaType(group.instances(), name, variants);
            }
            case ANY_OF_EMPTY -> {
                logger.warn("AnyOf schema without items");
                yield new EmptySchemaType(group.instances());
            }
            case ANY_OF_SINGLE -> {
                final var single = group.schema().getAnyOf().getFirst();
                yield new DeferredSchemaType(group.instances(), single);
            }
            case ANY_OF_MULTI -> new EmptySchemaType(group.instances());
            case INVALID -> {
                logger.warn("Found invalid schema:" + group.schema());
                yield new EmptySchemaType(group.instances());
            }
        };
    }

    private SchemaInstance resolveItemInstance(Schema<?> schema) {
        Schema<?> itemsSchema = schema.getItems();
        if (itemsSchema == null) {
            throw new IllegalStateException("Array schema is missing items schema.");
        }
        return registry.instanceForSchema(itemsSchema);
    }

    private List<SchemaInstance> resolveVariantInstances(Schema<?> schema) {
        List<SchemaInstance> variants = new ArrayList<>();
        if (schema.getOneOf() != null) {
            for (Schema<?> oneOfSchema : schema.getOneOf()) {
                variants.add(registry.instanceForSchema(oneOfSchema));
            }
        }
        if (schema.getAnyOf() != null) {
            for (Schema<?> anyOfSchema : schema.getAnyOf()) {
                variants.add(registry.instanceForSchema(anyOfSchema));
            }
        }
        return variants;
    }
}
