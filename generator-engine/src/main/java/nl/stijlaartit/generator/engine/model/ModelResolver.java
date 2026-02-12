package nl.stijlaartit.generator.engine.model;

import nl.stijlaartit.generator.engine.domain.EnumModel;
import nl.stijlaartit.generator.engine.domain.EnumValueType;
import nl.stijlaartit.generator.engine.domain.FieldModel;
import nl.stijlaartit.generator.engine.domain.GenerationContext;
import nl.stijlaartit.generator.engine.domain.ModelFile;
import nl.stijlaartit.generator.engine.domain.OneOfModel;
import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.RecordModel;
import nl.stijlaartit.generator.engine.domain.Resolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves OpenAPI schemas into a list of {@link ModelFile}s.
 *
 * <h2>Naming rules</h2>
 * <ol>
 *   <li>Named component schemas ({@code #/components/schemas/Foo}) use their component name.</li>
 *   <li>Anonymous inline object schemas are named by joining the parent name and the property name
 *       in PascalCase (e.g., parent "User" + property "address" = "UserAddress").</li>
 *   <li>If two anonymous schemas have the same shape (same fields, types, required set),
 *       the second one reuses the model generated for the first.</li>
 *   <li>If a named (component) schema has the same shape as an anonymous one,
 *       the component name takes priority.</li>
 * </ol>
 */
public class ModelResolver implements Resolver<OpenAPI> {

    /** Signature -> model name that was created for it */
    private final Map<SchemaSignature, String> signatureToName = new LinkedHashMap<>();
    private final Map<EnumSignature, String> enumSignatureToName = new LinkedHashMap<>();

    /** All resolved models, keyed by name */
    private final Map<String, ModelFile> models = new LinkedHashMap<>();

    /** Names that originated from component schemas (not anonymous inline objects) */
    private final Set<String> componentNames = new HashSet<>();
    private Map<String, Schema> componentSchemas = Map.of();
    private final Map<String, Set<String>> pendingImplements = new LinkedHashMap<>();

    @Override
    public void resolve(OpenAPI openAPI, GenerationContext context) {
        signatureToName.clear();
        enumSignatureToName.clear();
        models.clear();
        componentNames.clear();
        componentSchemas = Map.of();
        pendingImplements.clear();

        // First pass: component schemas (named schemas take priority)
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            componentSchemas = openAPI.getComponents().getSchemas();
            for (var entry : openAPI.getComponents().getSchemas().entrySet()) {
                String modelName = normalizeModelName(entry.getKey());
                resolveSchema(modelName, entry.getValue(), true);
            }
        }

        if (openAPI.getPaths() != null) {
            for (var pathEntry : openAPI.getPaths().entrySet()) {
                resolveOperationSchemas(pathEntry.getKey(), pathEntry.getValue());
            }
        }

        context.addFiles(List.copyOf(models.values()));
    }

    private void resolveOperationSchemas(String path, io.swagger.v3.oas.models.PathItem pathItem) {
        resolveOperationSchema(path, io.swagger.v3.oas.models.PathItem.HttpMethod.GET, pathItem.getGet());
        resolveOperationSchema(path, io.swagger.v3.oas.models.PathItem.HttpMethod.POST, pathItem.getPost());
        resolveOperationSchema(path, io.swagger.v3.oas.models.PathItem.HttpMethod.PUT, pathItem.getPut());
        resolveOperationSchema(path, io.swagger.v3.oas.models.PathItem.HttpMethod.DELETE, pathItem.getDelete());
        resolveOperationSchema(path, io.swagger.v3.oas.models.PathItem.HttpMethod.PATCH, pathItem.getPatch());
    }

    private void resolveOperationSchema(String path,
                                        io.swagger.v3.oas.models.PathItem.HttpMethod method,
                                        io.swagger.v3.oas.models.Operation operation) {
        if (operation == null) {
            return;
        }

        String operationId = resolveOperationId(operation.getOperationId(), method, path);
        String baseName = toPascalCase(operationId);
        if (baseName == null || baseName.isBlank()) {
            baseName = "Operation";
        }

        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Schema<?> requestSchema = resolveContentSchema(operation.getRequestBody().getContent());
            resolveInlineSchema(baseName + "Request", requestSchema);
        }

        if (operation.getResponses() != null && operation.getResponses().get("200") != null) {
            var response = operation.getResponses().get("200");
            if (response.getContent() != null) {
                Schema<?> responseSchema = resolveContentSchema(response.getContent());
                resolveInlineSchema(baseName + "Response", responseSchema);
            }
        }
    }

    private static String resolveOperationId(String operationId,
                                             io.swagger.v3.oas.models.PathItem.HttpMethod method,
                                             String path) {
        if (operationId != null && !operationId.isBlank()) {
            return operationId;
        }
        return fallbackOperationId(method, path);
    }

    private static String fallbackOperationId(io.swagger.v3.oas.models.PathItem.HttpMethod method, String path) {
        String normalized = path == null ? "" : path;
        normalized = normalized.replace("{", "").replace("}", "");
        normalized = normalized.replaceAll("[^A-Za-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        String methodPrefix = method != null ? method.name().toLowerCase() : "operation";
        if (normalized.isBlank()) {
            return methodPrefix;
        }
        return methodPrefix + "_" + normalized;
    }

    private Schema<?> resolveContentSchema(io.swagger.v3.oas.models.media.Content content) {
        if (content == null) {
            return null;
        }
        io.swagger.v3.oas.models.media.MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = content.values().iterator().next();
        }
        if (mediaType == null) {
            return null;
        }
        return mediaType.getSchema();
    }

    private void resolveInlineSchema(String name, Schema<?> schema) {
        if (schema == null) {
            return;
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            if (schema.getAllOf().size() == 1) {
                resolveInlineSchema(name, schema.getAllOf().get(0));
                return;
            }
        }

        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            if (resolveOneOfSchema(name, schema, false) != null) {
                return;
            }
        }

        if (schema.get$ref() != null) {
            return;
        }

        if (isEnumSchema(schema)) {
            createOperationEnumModel(name, schema);
            return;
        }

        if (isObjectSchema(schema)) {
            createOperationRecordModel(name, schema);
            return;
        }

        String type = resolveSchemaTypeName(schema);
        if ("array".equals(type) && schema.getItems() != null) {
            resolveInlineSchema(name + "Item", schema.getItems());
            return;
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            resolveInlineSchema(name + "Value", additional);
        }
    }

    private void createOperationRecordModel(String name, Schema<?> schema) {
        if (models.containsKey(name)) {
            return;
        }
        List<FieldModel> fields = resolveFields(name, schema);
        RecordModel model = new RecordModel(name, fields, consumePendingImplements(name));
        models.put(name, model);
    }

    private void createOperationEnumModel(String name, Schema<?> schema) {
        if (models.containsKey(name)) {
            return;
        }
        List<String> values = extractEnumValues(schema);
        EnumModel model = new EnumModel(name, values, enumValueType(schema), consumePendingImplements(name));
        models.put(name, model);
    }

    /**
     * Resolves a schema into a model. Returns the model name if this schema represents
     * a complex object type, or null if it maps to a simple/built-in Java type.
     */
    private String resolveSchema(String name, Schema<?> schema, boolean isComponent) {
        if (schema.get$ref() != null) {
            return normalizeModelName(extractRefName(schema.get$ref()));
        }

        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            String resolved = resolveOneOfSchema(name, schema, isComponent);
            if (resolved != null) {
                return resolved;
            }
        }

        if (isEnumSchema(schema)) {
            return resolveEnumSchema(name, schema, isComponent);
        }

        if (!isObjectSchema(schema)) {
            return null;
        }

        SchemaSignature signature = SchemaSignature.of(schema);

        // If a model with this signature already exists...
        String existingName = signatureToName.get(signature);
        if (existingName != null) {
            if (!isComponent) {
                // Anonymous schemas always reuse existing models with the same shape
                return existingName;
            }
            if (!componentNames.contains(existingName)) {
                // Component schema takes priority: replace anonymous name with component name
                ModelFile existing = models.remove(existingName);
                if (existing instanceof RecordModel record) {
                    RecordModel renamed = new RecordModel(name, record.getFields(), List.of());
                    models.put(name, renamed);
                    signatureToName.put(signature, name);
                    componentNames.add(name);
                    return name;
                }
            }
            // Existing is also a component — both must exist, so fall through to create new model
        }

        // Build field descriptors (this may recursively resolve nested anonymous schemas)
        List<FieldModel> fields = resolveFields(name, schema);

        RecordModel model = new RecordModel(name, fields, consumePendingImplements(name));
        models.put(name, model);
        signatureToName.put(signature, name);
        if (isComponent) {
            componentNames.add(name);
        }
        return name;
    }

    private List<FieldModel> resolveFields(String parentName, Schema<?> schema) {
        Map<String, Schema> properties = collectProperties(schema);
        if (properties.isEmpty() && hasAdditionalProperties(schema)) {
            TypeDescriptor valueType = resolveAdditionalPropertiesType(parentName, schema);
            return List.of(new FieldModel("value", "value", valueType, true, true));
        }
        if (properties.isEmpty()) {
            return List.of();
        }

        Set<String> required = collectRequired(schema);

        List<FieldModel> fields = new ArrayList<>();
        for (var entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            Schema<?> propertySchema = entry.getValue();

            TypeDescriptor type = resolveType(parentName, propertyName, propertySchema);
            String javaName = JavaIdentifierUtils.sanitize(toCamelCase(propertyName));

            fields.add(new FieldModel(javaName, propertyName, type, required.contains(propertyName)));
        }
        return fields;
    }

    TypeDescriptor resolveType(String parentName, String propertyName, Schema<?> schema) {
        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            if (schema.getAllOf().size() == 1) {
                return resolveType(parentName, propertyName, schema.getAllOf().get(0));
            }
        }

        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            String oneOfName = parentName + toPascalCase(propertyName);
            String resolvedName = resolveOneOfSchema(oneOfName, schema, false);
            if (resolvedName != null) {
                return TypeDescriptor.complex(resolvedName);
            }
        }

        if (schema.get$ref() != null) {
            String refName = extractRefName(schema.get$ref());
            Schema<?> refSchema = componentSchemas.get(refName);
            if (refSchema != null) {
                if (isEnumSchema(refSchema) || isObjectSchema(refSchema)) {
                    return TypeDescriptor.complex(normalizeModelName(refName));
                }
                return resolveType(parentName, propertyName, refSchema);
            }
            return TypeDescriptor.complex(normalizeModelName(refName));
        }

        if (isEnumSchema(schema)) {
            String enumName = parentName + toPascalCase(propertyName);
            String resolvedName = resolveEnumSchema(enumName, schema, false);
            return TypeDescriptor.complex(resolvedName);
        }

        String type = resolveSchemaTypeName(schema);

        if ("array".equals(type) && schema.getItems() != null) {
            TypeDescriptor elementType = resolveType(parentName, propertyName, schema.getItems());
            return TypeDescriptor.list(elementType);
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            TypeDescriptor valueType = resolveType(parentName, propertyName, additional);
            return TypeDescriptor.map(valueType);
        }
        if (Boolean.TRUE.equals(schema.getAdditionalProperties())) {
            return TypeDescriptor.map(TypeDescriptor.simple("java.lang.Object"));
        }

        if (isObjectSchema(schema)) {
            // Anonymous nested object - generate an intermediate model
            String nestedName = parentName + toPascalCase(propertyName);
            String resolvedName = resolveSchema(nestedName, schema, false);
            return TypeDescriptor.complex(resolvedName);
        }

        return mapSimpleType(type, schema.getFormat());
    }

    private String resolveEnumSchema(String name, Schema<?> schema, boolean isComponent) {
        EnumSignature signature = EnumSignature.of(schema);

        String existingName = enumSignatureToName.get(signature);
        if (existingName != null) {
            if (!isComponent) {
                return existingName;
            }
            if (!componentNames.contains(existingName)) {
                ModelFile existing = models.remove(existingName);
                if (existing instanceof EnumModel enumDescriptor) {
                    EnumModel renamed = new EnumModel(
                            name,
                            enumDescriptor.getEnumValues(),
                            enumDescriptor.getEnumValueType(),
                            enumDescriptor.getImplementsTypes()
                    );
                    models.put(name, renamed);
                    enumSignatureToName.put(signature, name);
                    componentNames.add(name);
                    return name;
                }
            }
        }

        List<String> values = extractEnumValues(schema);
        EnumModel model = new EnumModel(name, values, enumValueType(schema), consumePendingImplements(name));
        models.put(name, model);
        enumSignatureToName.put(signature, name);
        if (isComponent) {
            componentNames.add(name);
        }
        return name;
    }

    private String resolveOneOfSchema(String name, Schema<?> schema, boolean isComponent) {
        List<Schema> variants = schema.getOneOf();
        if (variants == null || variants.isEmpty()) {
            return null;
        }

        if (models.containsKey(name)) {
            return name;
        }

        String discriminatorProperty = schema.getDiscriminator() != null
                ? schema.getDiscriminator().getPropertyName()
                : null;
        if (discriminatorProperty == null || discriminatorProperty.isBlank()) {
            discriminatorProperty = inferDiscriminatorProperty(variants);
        }

        List<OneOfVariant> variantModels = new ArrayList<>();
        int index = 1;
        for (Schema variant : variants) {
            String variantName = resolveOneOfVariant(name, index++, (Schema<?>) variant);
            if (variantName == null) {
                return null;
            }
            String discriminatorValue = resolveDiscriminatorValue(variant, discriminatorProperty);
            variantModels.add(new OneOfVariant(variantName, discriminatorValue));
        }

        OneOfModel model = new OneOfModel(name, variantModels, discriminatorProperty);
        models.put(name, model);
        if (isComponent) {
            componentNames.add(name);
        }
        for (OneOfVariant variant : variantModels) {
            addInterfaceImplementation(variant.getModelName(), name);
        }
        return name;
    }

    private String resolveOneOfVariant(String baseName, int index, Schema<?> variant) {
        if (variant.getAllOf() != null && !variant.getAllOf().isEmpty()) {
            if (variant.getAllOf().size() == 1) {
                return resolveOneOfVariant(baseName, index, variant.getAllOf().get(0));
            }
        }
        if (variant.get$ref() != null) {
            return normalizeModelName(extractRefName(variant.get$ref()));
        }
        if (isEnumSchema(variant)) {
            String enumName = baseName + "Option" + index;
            resolveEnumSchema(enumName, variant, false);
            return enumName;
        }
        if (isObjectSchema(variant)) {
            String recordName = baseName + "Option" + index;
            resolveSchema(recordName, variant, false);
            return recordName;
        }
        return null;
    }

    private void addInterfaceImplementation(String modelName, String interfaceName) {
        if (modelName == null || interfaceName == null) {
            return;
        }
        ModelFile existing = models.get(modelName);
        if (existing instanceof RecordModel record) {
            models.put(modelName, new RecordModel(
                    record.getName(),
                    record.getFields(),
                    mergeInterfaces(record.getImplementsTypes(), interfaceName)
            ));
            return;
        }
        if (existing instanceof EnumModel enumDescriptor) {
            models.put(modelName, new EnumModel(
                    enumDescriptor.getName(),
                    enumDescriptor.getEnumValues(),
                    enumDescriptor.getEnumValueType(),
                    mergeInterfaces(enumDescriptor.getImplementsTypes(), interfaceName)
            ));
            return;
        }
        pendingImplements
                .computeIfAbsent(modelName, key -> new LinkedHashSet<>())
                .add(interfaceName);
    }

    private List<String> consumePendingImplements(String modelName) {
        Set<String> pending = pendingImplements.remove(modelName);
        if (pending == null || pending.isEmpty()) {
            return List.of();
        }
        return List.copyOf(pending);
    }

    private static List<String> mergeInterfaces(List<String> existing, String additional) {
        LinkedHashSet<String> merged = new LinkedHashSet<>(existing);
        merged.add(additional);
        return List.copyOf(merged);
    }

    private String resolveDiscriminatorValue(Schema<?> variant, String discriminatorProperty) {
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
            return String.valueOf(resolvedProperty.getEnum().get(0));
        }
        return null;
    }

    private String inferDiscriminatorProperty(List<Schema> variants) {
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

    static TypeDescriptor mapSimpleType(String type, String format) {
        if (type == null) {
            return TypeDescriptor.simple("java.lang.Object");
        }

        return switch (type) {
            case "string" -> mapStringType(format);
            case "integer" -> "int64".equals(format)
                    ? TypeDescriptor.simple("java.lang.Long")
                    : TypeDescriptor.simple("java.lang.Integer");
            case "number" -> switch (format != null ? format : "") {
                case "float" -> TypeDescriptor.simple("java.lang.Float");
                case "double" -> TypeDescriptor.simple("java.lang.Double");
                default -> TypeDescriptor.simple("java.math.BigDecimal");
            };
            case "boolean" -> TypeDescriptor.simple("java.lang.Boolean");
            default -> TypeDescriptor.simple("java.lang.Object");
        };
    }

    private static TypeDescriptor mapStringType(String format) {
        if (format == null) {
            return TypeDescriptor.simple("java.lang.String");
        }
        return switch (format) {
            case "date" -> TypeDescriptor.simple("java.time.LocalDate");
            case "date-time" -> TypeDescriptor.simple("java.time.OffsetDateTime");
            case "uuid" -> TypeDescriptor.simple("java.util.UUID");
            default -> TypeDescriptor.simple("java.lang.String");
        };
    }

    private boolean isObjectSchema(Schema<?> schema) {
        String type = resolveSchemaTypeName(schema);
        if (!("object".equals(type) || type == null)) {
            return false;
        }
        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            return true;
        }
        if (hasAdditionalProperties(schema)) {
            return true;
        }
        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            return !collectProperties(schema).isEmpty();
        }
        return false;
    }

    private static boolean hasAdditionalProperties(Schema<?> schema) {
        return schema.getAdditionalProperties() instanceof Schema<?>
                || Boolean.TRUE.equals(schema.getAdditionalProperties());
    }

    private boolean isAdditionalPropertiesObject(Schema<?> schema) {
        if (!hasAdditionalProperties(schema)) {
            return false;
        }
        String type = resolveSchemaTypeName(schema);
        if (!("object".equals(type) || type == null)) {
            return false;
        }
        return collectProperties(schema).isEmpty();
    }

    private TypeDescriptor resolveAdditionalPropertiesType(String parentName, Schema<?> schema) {
        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            TypeDescriptor valueType = resolveType(parentName, "value", additional);
            return TypeDescriptor.map(valueType);
        }
        return TypeDescriptor.map(TypeDescriptor.simple("java.lang.Object"));
    }

    private static boolean isEnumSchema(Schema<?> schema) {
        return schema.getEnum() != null && !schema.getEnum().isEmpty();
    }

    private static List<String> extractEnumValues(Schema<?> schema) {
        return schema.getEnum().stream()
                .map(String::valueOf)
                .toList();
    }

    private static EnumValueType enumValueType(Schema<?> schema) {
        String type = resolveSchemaTypeName(schema);
        if ("integer".equals(type)) {
            return EnumValueType.INTEGER;
        }
        if ("number".equals(type)) {
            return EnumValueType.NUMBER;
        }
        if ("boolean".equals(type)) {
            return EnumValueType.BOOLEAN;
        }
        return EnumValueType.STRING;
    }

    private static String resolveSchemaTypeName(Schema<?> schema) {
        String type = schema.getType();
        if (type != null) {
            return type;
        }
        if (schema.getTypes() != null && !schema.getTypes().isEmpty()) {
            if (schema.getTypes().contains("string")) {
                return "string";
            }
            if (schema.getTypes().contains("integer")) {
                return "integer";
            }
            if (schema.getTypes().contains("number")) {
                return "number";
            }
            if (schema.getTypes().contains("boolean")) {
                return "boolean";
            }
            if (schema.getTypes().contains("array")) {
                return "array";
            }
            if (schema.getTypes().contains("object")) {
                return "object";
            }
            return schema.getTypes().iterator().next();
        }
        if (schema instanceof StringSchema) {
            return "string";
        }
        if (schema instanceof IntegerSchema) {
            return "integer";
        }
        if (schema instanceof NumberSchema) {
            return "number";
        }
        if (schema instanceof BooleanSchema) {
            return "boolean";
        }
        if (schema instanceof ArraySchema) {
            return "array";
        }
        if (schema instanceof ObjectSchema) {
            return "object";
        }
        return null;
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
        Set<String> required = new HashSet<>();
        if (schema.getAllOf() != null) {
            for (Schema<?> part : schema.getAllOf()) {
                Schema<?> resolved = resolveRefSchema(part);
                required.addAll(collectRequired(resolved));
            }
        }
        if (schema.getRequired() != null) {
            required.addAll(schema.getRequired());
        }
        return Set.copyOf(required);
    }

    private Schema<?> resolveRefSchema(Schema<?> schema) {
        if (schema.get$ref() == null) {
            return schema;
        }
        String refName = extractRefName(schema.get$ref());
        return componentSchemas.getOrDefault(refName, schema);
    }

    private static String normalizeModelName(String name) {
        String pascal = toPascalCase(name);
        String sanitized = JavaIdentifierUtils.sanitize(pascal);
        if (sanitized.isEmpty()) {
            return "Model";
        }
        if (Character.isLowerCase(sanitized.charAt(0))) {
            sanitized = Character.toUpperCase(sanitized.charAt(0)) + sanitized.substring(1);
        }
        return sanitized;
    }

    static String extractRefName(String ref) {
        int lastSlash = ref.lastIndexOf('/');
        return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
    }

    static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_' || c == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else if (i == 0) {
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String camel = toCamelCase(input);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }
}
