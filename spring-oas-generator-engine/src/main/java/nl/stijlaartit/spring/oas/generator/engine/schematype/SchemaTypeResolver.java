package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaParent;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaRegistry;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.findShortestComponentPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.findShortestOperationPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.hasComponentPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.hasOperationPath;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.toPascalCase;
import static nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil.validateName;

public final class SchemaTypeResolver {

    private final SchemaRegistry registry;

    public SchemaTypeResolver(SchemaRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public SchemaTypes resolve() {

        List<SchemaInstanceGroup> groups = SchemaInstanceGroup.groupBySchemaEquals(registry.getInstances());
        Set<String> usedNames = new LinkedHashSet<>();
        List<SchemaType> types = new ArrayList<>();

        for (SchemaInstanceGroup group : groups) {
            SchemaType type = createType(group, usedNames);
            types.add(type);
        }


        return new SchemaTypes(types);
    }

    private SchemaType createType(SchemaInstanceGroup group,
                                  Set<String> usedNames) {
        Schema<?> schema = group.schema();
        Schema<?> allOfRef = isComponentSchema(group.instances()) ? null : unwrapSingleAllOfRef(schema);
        if (allOfRef != null) {
            return new RefSchemaType(group.instances(), allOfRef.get$ref());
        }
        if (schema.get$ref() != null && !schema.get$ref().isBlank()) {
            return new RefSchemaType(group.instances(), schema.get$ref());
        }
        if (isUnionSchema(schema)) {
            String name = resolveUniqueName(group.instances(), "InlineUnion", usedNames);
            List<SchemaInstance> variants = resolveVariantInstances(schema);
            return new UnionSchemaType(group.instances(), name, variants);
        }
        if (isEnumSchema(schema)) {
            String name = resolveUniqueName(group.instances(), "InlineEnum", usedNames);
            return new EnumSchemaType(group.instances(), name);
        }
        if (isArraySchema(schema)) {
            SchemaInstance itemInstance = resolveItemInstance(schema);
            return new ListSchemaType(group.instances(), itemInstance);
        }
        if (isMapSchema(schema)) {
            if (isOperationSchema(group.instances()) || isComponentSchema(group.instances())) {
                String name = resolveUniqueName(group.instances(), "InlineObject", usedNames);
                return new ObjectSchemaType(group.instances(), name);
            }
            SchemaInstance valueInstance = resolveAdditionalPropertiesInstance(schema);
            return new MapSchemaType(group.instances(), valueInstance);
        }
        if (isObjectSchema(schema)) {
            String name = resolveUniqueName(group.instances(), "InlineObject", usedNames);
            return new ObjectSchemaType(group.instances(), name);
        }
        return createPrimitiveType(group.instances(), schema);
    }

    private Schema<?> unwrapSingleAllOfRef(Schema<?> schema) {
        if (schema.getAllOf() == null || schema.getAllOf().size() != 1) {
            return null;
        }
        if (hasNonAllOfConstraints(schema)) {
            return null;
        }
        Schema<?> candidate = schema.getAllOf().getFirst();
        if (candidate == null || candidate.get$ref() == null || candidate.get$ref().isBlank()) {
            return null;
        }
        return candidate;
    }

    private boolean hasNonAllOfConstraints(Schema<?> schema) {
        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            return true;
        }
        if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            return true;
        }
        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            return true;
        }
        if (schema.getRequired() != null && !schema.getRequired().isEmpty()) {
            return true;
        }
        if (schema.getAdditionalProperties() != null) {
            return true;
        }
        if (schema.getItems() != null) {
            return true;
        }
        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean isComponentSchema(List<SchemaInstance> instances) {
        for (SchemaInstance instance : instances) {
            if (instance.parent() instanceof SchemaParent.ComponentParent) {
                return true;
            }
            if (instance.parent() instanceof SchemaParent.ComponentParameterParent) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperationSchema(List<SchemaInstance> instances) {
        for (SchemaInstance instance : instances) {
            if (instance.parent() instanceof SchemaParent.OperationRequestParent) {
                return true;
            }
            if (instance.parent() instanceof SchemaParent.OperationResponseParent) {
                return true;
            }
            if (instance.parent() instanceof SchemaParent.OperationParameterParent) {
                return true;
            }
        }
        return false;
    }

    private SchemaType createPrimitiveType(List<SchemaInstance> instances,
                                           Schema<?> schema) {
        String type = SchemaUtil.schemaTypeName(schema);
        return switch (type != null ? type : "") {
            case "string" -> new StringSchemaType(instances);
            case "integer" -> new IntegerSchemaType(instances);
            case "number" -> new DecimalSchemaType(instances);
            case "boolean" -> new BooleanSchemaType(instances);
            default -> {
                if (schema.equals(new Schema<>()) || schema.equals(new JsonSchema())) {
                   yield new EmptySchemaType(instances);
                } else {
                    // TODO Warning
                    yield new EmptySchemaType(instances);
                }
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

    private SchemaInstance resolveAdditionalPropertiesInstance(Schema<?> schema) {
        if (!(schema.getAdditionalProperties() instanceof Schema<?> additional)) {
            throw new IllegalStateException("Map schema is missing additionalProperties schema.");
        }
        return registry.instanceForSchema(additional);
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

    private String resolveUniqueName(List<SchemaInstance> instances, String inlineName, Set<String> usedNames) {
        String baseName = resolveName(instances, inlineName);
        String uniqueName = baseName;
        int suffix = 2;
        while (usedNames.contains(uniqueName)) {
            uniqueName = baseName + suffix++;
        }
        usedNames.add(uniqueName);
        return uniqueName;
    }

    private String resolveName(List<SchemaInstance> instances, String inlineName) {
        for (SchemaInstance instance : instances) {
            if (instance.parent() instanceof SchemaParent.ComponentParent(String componentName)) {
                return validateName(toPascalCase(componentName));
            }
        }

        if (hasComponentPath(instances)) {
            NamingUtil.PathName componentPath = findShortestComponentPath(instances);
            return validateName(componentPath.toName());
        }

        if (hasOperationPath(instances)) {
            NamingUtil.PathName operationPath = findShortestOperationPath(instances);
            return validateName(operationPath.toName());
        }

        return validateName(inlineName);
    }

    private boolean isUnionSchema(Schema<?> schema) {
        return (schema.getOneOf() != null && !schema.getOneOf().isEmpty())
                || (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty());
    }

    private boolean isEnumSchema(Schema<?> schema) {
        return schema.getEnum() != null && !schema.getEnum().isEmpty();
    }

    private boolean isArraySchema(Schema<?> schema) {
        String type = SchemaUtil.schemaTypeName(schema);
        return "array".equals(type) || schema instanceof ArraySchema;
    }

    private boolean isMapSchema(Schema<?> schema) {
        return schema.getAdditionalProperties() instanceof Schema<?>;
    }

    private boolean isObjectSchema(Schema<?> schema) {
        String type = SchemaUtil.schemaTypeName(schema);
        if ("object".equals(type) || schema instanceof ObjectSchema) {
            return true;
        }
        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            return true;
        }
        return schema.getProperties() != null && !schema.getProperties().isEmpty();
    }
}
