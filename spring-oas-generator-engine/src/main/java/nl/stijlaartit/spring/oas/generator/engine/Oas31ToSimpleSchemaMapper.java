package nl.stijlaartit.spring.oas.generator.engine;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.IntegerEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.NumberEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ParamIn;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.RefSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleAnySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBinarySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBooleanSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleIntegerSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleLongSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleNumberSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleReponse;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ResponseMediaType;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleStringSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.StringEnumSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOperation;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.math.BigDecimal;

/**
 * Maps OpenAPI 3.1 models into the simplified schema domain.
 *
 * <p>This mapper intentionally does not use {@code Schema#getNullable()}, because that flag is an
 * OpenAPI 3.0 mechanism. For 3.1, nullability is derived from schema types:
 * {@code getTypes()} (preferred), falling back to {@code getType()} when needed.
 */
public final class Oas31ToSimpleSchemaMapper {
    private final Logger logger;

    public Oas31ToSimpleSchemaMapper() {
        this(Logger.noOp());
    }

    public Oas31ToSimpleSchemaMapper(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    public SimplifiedOas resolve(OpenAPI openAPI) {
        Objects.requireNonNull(openAPI);

        final Map<String, SimpleSchema> componentSchemas = resolveComponentSchemas(openAPI);
        final Map<String, SimpleSchema> componentParameters = resolveComponentParameters(openAPI);
        final Map<String, List<SimpleParam>> pathParams = new LinkedHashMap<>();
        final List<SimplifiedOperation> operations = resolveOperations(openAPI, pathParams);

        return new SimplifiedOas(componentSchemas, componentParameters, operations, pathParams);
    }

    private Map<String, SimpleSchema> resolveComponentSchemas(OpenAPI openAPI) {
        final Map<String, SimpleSchema> mapped = new LinkedHashMap<>();
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) {
            return mapped;
        }

        for (var entry : openAPI.getComponents().getSchemas().entrySet()) {
            final Schema<?> schema = entry.getValue();
            if (schema == null) {
                continue;
            }
            final var path = SchemaPath.forRoot(PathRoot.componentSchema(entry.getKey()));
            mapped.put(entry.getKey(), mapSchema(schema, path));
        }
        return mapped;
    }

    private Map<String, SimpleSchema> resolveComponentParameters(OpenAPI openAPI) {
        final Map<String, SimpleSchema> mapped = new LinkedHashMap<>();
        if (openAPI.getComponents() == null || openAPI.getComponents().getParameters() == null) {
            return mapped;
        }

        for (var entry : openAPI.getComponents().getParameters().entrySet()) {
            final Parameter parameter = entry.getValue();
            if (parameter == null) {
                continue;
            }
            final Parameter resolved = resolveParameterRef(openAPI, parameter);
            if (resolved == null || resolved.getSchema() == null) {
                continue;
            }

            final var path = SchemaPath.forRoot(PathRoot.componentParameter(entry.getKey()));
            mapped.put(entry.getKey(), mapSchema(resolved.getSchema(), path));
        }
        return mapped;
    }

    private List<SimplifiedOperation> resolveOperations(OpenAPI openAPI, Map<String, List<SimpleParam>> pathParams) {
        final List<SimplifiedOperation> operations = new ArrayList<>();
        if (openAPI.getPaths() == null) {
            return operations;
        }

        for (var entry : openAPI.getPaths().entrySet()) {
            final String path = entry.getKey();
            final PathItem pathItem = entry.getValue();
            if (pathItem == null) {
                continue;
            }

            final List<SimpleParam> sharedPathParams = mapParams(openAPI, pathItem.getParameters(), true);
            if (!sharedPathParams.isEmpty()) {
                pathParams.put(path, sharedPathParams);
            }

            addOperation(openAPI, operations, path, HttpMethod.GET, pathItem.getGet());
            addOperation(openAPI, operations, path, HttpMethod.POST, pathItem.getPost());
            addOperation(openAPI, operations, path, HttpMethod.PUT, pathItem.getPut());
            addOperation(openAPI, operations, path, HttpMethod.DELETE, pathItem.getDelete());
            addOperation(openAPI, operations, path, HttpMethod.PATCH, pathItem.getPatch());
        }

        return operations;
    }

    private void addOperation(OpenAPI openAPI,
                              List<SimplifiedOperation> operations,
                              String path,
                              HttpMethod method,
                              @Nullable Operation operation) {
        if (operation == null) {
            return;
        }

        final String operationId = operation.getOperationId();
        final OperationName operationName = operationName(path, method, operationId);
        final Set<String> tags = (operation.getTags() == null || operation.getTags().isEmpty())
                ? Set.of("default")
                : new LinkedHashSet<>(operation.getTags());
        final List<SimpleParam> params = mapParams(openAPI, operation.getParameters(), false);
        final List<SimpleReponse> responses = mapResponses(operationName, operation.getResponses());
        final SimpleSchema requestBody = mapRequestBody(operationName, operation.getRequestBody());

        operations.add(new SimplifiedOperation(path, method, operationId, tags, params, responses, requestBody));
    }

    private List<SimpleReponse> mapResponses(OperationName operationName, @Nullable ApiResponses responses) {
        if (responses == null) {
            return List.of();
        }

        final List<SimpleReponse> mapped = new ArrayList<>();
        for (var entry : responses.entrySet()) {
            final String status = entry.getKey();
            final var response = entry.getValue();
            if (response == null) {
                continue;
            }
            final List<ResolvedContent> resolvedContents = resolveContentSchemas(response.getContent());
            for (ResolvedContent resolvedContent : resolvedContents) {
                final SchemaPath path = SchemaPath.forRoot(PathRoot.responseBody(operationName, status));
                final SimpleSchema schema = resolvedContent.mediaType() == ResponseMediaType.APPLICATION_OCTET_STREAM
                        && isBinaryStringSchema(resolvedContent.schema())
                        ? new SimpleBinarySchema(isNullable(resolvedContent.schema()))
                        : mapSchema(resolvedContent.schema(), path);
                mapped.add(new SimpleReponse(status, schema, resolvedContent.mediaType()));
            }
        }
        return mapped;
    }

    @Nullable
    private SimpleSchema mapRequestBody(OperationName operationName, @Nullable RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }
        final ResolvedContent resolvedContent = resolveContentSchema(requestBody.getContent());
        if (resolvedContent == null) {
            return null;
        }
        if (resolvedContent.mediaType() == ResponseMediaType.APPLICATION_OCTET_STREAM
                && isBinaryStringSchema(resolvedContent.schema())) {
            return new SimpleBinarySchema(isNullable(resolvedContent.schema()));
        }
        final var path = SchemaPath.forRoot(PathRoot.requestBody(operationName));
        return mapSchema(resolvedContent.schema(), path);
    }

    private List<SimpleParam> mapParams(OpenAPI openAPI, @Nullable List<Parameter> parameters, boolean pathOnly) {
        if (parameters == null || parameters.isEmpty()) {
            return List.of();
        }
        final List<SimpleParam> mapped = new ArrayList<>();
        for (Parameter parameter : parameters) {
            final Parameter resolved = resolveParameterRef(openAPI, parameter);
            if (resolved == null) {
                continue;
            }
            final String name = resolved.getName();
            final String in = resolved.getIn();
            if (name == null || in == null) {
                throw new IllegalArgumentException("Parameter must define both name and in.");
            }

            final ParamIn paramIn = switch (in) {
                case "query" -> ParamIn.Query;
                case "path" -> ParamIn.Path;
                case "header" -> ParamIn.Header;
                default -> null;
            };

            if (paramIn == null) {
                continue;
            }
            if (pathOnly && paramIn != ParamIn.Path) {
                continue;
            }
            final SimpleSchema paramSchema = resolved.getSchema() == null
                    ? new SimpleAnySchema(false)
                    : mapSchema(resolved.getSchema(), SchemaPath.forRoot(PathRoot.componentParameter(name)));
            final boolean required = paramIn == ParamIn.Path || Boolean.TRUE.equals(resolved.getRequired());
            mapped.add(new SimpleParam(name, paramIn, paramSchema, required));
        }
        return mapped;
    }

    private Parameter resolveParameterRef(OpenAPI openAPI, Parameter parameter) {
        if (parameter.get$ref() == null) {
            return parameter;
        }

        final Map<String, Parameter> componentParameters = openAPI.getComponents() == null
                || openAPI.getComponents().getParameters() == null
                ? Map.of()
                : openAPI.getComponents().getParameters();

        Parameter current = parameter;
        Set<SchemaRef> visitedRefs = new HashSet<>();
        while (current.get$ref() != null) {
            final SchemaRef ref = SchemaRef.parseFromRefValue(current.get$ref());
            if (!"parameters".equals(ref.type())) {
                throw new IllegalArgumentException("Only component parameter refs are supported: " + current.get$ref());
            }
            if (!visitedRefs.add(ref)) {
                throw new IllegalArgumentException("Circular parameter reference detected: " + ref.name());
            }
            current = componentParameters.get(ref.name());
            if (current == null) {
                throw new IllegalArgumentException("Parameter reference not found: " + ref.name());
            }
        }
        return current;
    }

    private SimpleSchema mapSchema(Schema<?> schema, SchemaPath path) {
        if (schema.get$ref() != null) {
            return new RefSchema(isNullable(schema), SchemaRef.parseFromRefValue(schema.get$ref()));
        }

        if (usesMixedComposition(schema)) {
            logger.warn("Mixed schema composition (allOf/anyOf/oneOf) is not supported at path " + path + ". Falling back to SimpleAnySchema.");
            return new SimpleAnySchema(isNullable(schema));
        }

        final List<Schema> oneOfNonNull = nonNullVariants(schema.getOneOf());
        if (!oneOfNonNull.isEmpty()) {
            List<SimpleSchema> variants = new ArrayList<>();
            for (int i = 0; i < oneOfNonNull.size(); i++) {
                variants.add(mapSchema(oneOfNonNull.get(i), path.variant("oneOf", i)));
            }
            return new UnionSchema(
                    isNullable(schema),
                    variants,
                    discriminatorProperty(schema)
            );
        }

        final List<Schema> anyOfNonNull = nonNullVariants(schema.getAnyOf());
        if (!anyOfNonNull.isEmpty()) {
            List<SimpleSchema> components = new ArrayList<>();
            for (int i = 0; i < anyOfNonNull.size(); i++) {
                components.add(mapSchema(anyOfNonNull.get(i), path.variant("anyOf", i)));
            }
            return new CompositeSchema(isNullable(schema), components);
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            List<SimpleSchema> components = new ArrayList<>();
            for (int i = 0; i < schema.getAllOf().size(); i++) {
                final SchemaPath childPath = schema.getAllOf().size() == 1
                        ? path.singletonVariant("allOf")
                        : path.variant("allOf", i);
                components.add(mapSchema(schema.getAllOf().get(i), childPath));
            }
            return new CompositeSchema(isNullable(schema), components);
        }

        if (schema instanceof ArraySchema || hasType(schema, "array")) {
            final SimpleSchema itemSchema = schema.getItems() == null
                    ? new SimpleAnySchema(false)
                    : mapSchema(schema.getItems(), path.items());
            return new SimpleArraySchema(isNullable(schema), itemSchema);
        }

        if (schema instanceof ObjectSchema || hasType(schema, "object") || schema.getProperties() != null || schema.getAdditionalProperties() != null) {
            final List<ObjectProperty> properties = new ArrayList<>();
            final List<String> required = schema.getRequired() == null ? List.of() : schema.getRequired();
            final Set<String> requiredProperties = new LinkedHashSet<>(required);
            final Set<String> mappedPropertyNames = new HashSet<>();
            if (schema.getProperties() != null) {
                for (var entry : schema.getProperties().entrySet()) {
                    final String key = entry.getKey();
                    final Schema<?> propertySchema = entry.getValue();
                    final SimpleSchema mapped = mapSchema(propertySchema, path.property(key));
                    properties.add(new ObjectProperty(key, mapped));
                    mappedPropertyNames.add(key);
                }
            }
            for (String requiredProperty : required) {
                if (!mappedPropertyNames.contains(requiredProperty)) {
                    properties.add(new ObjectProperty(requiredProperty, new SimpleAnySchema(false)));
                }
            }

            final Optional<SimpleSchema> additionalProperties;
            if (schema.getAdditionalProperties() instanceof Schema<?> additionalSchema) {
                additionalProperties = Optional.of(mapSchema(additionalSchema, path.additionalProperties()));
            } else if (Boolean.TRUE.equals(schema.getAdditionalProperties())) {
                additionalProperties = Optional.of(new SimpleAnySchema(false));
            } else {
                additionalProperties = Optional.empty();
            }

            return new SimpleObjectSchema(isNullable(schema), properties, requiredProperties, additionalProperties);
        }

        if (schema instanceof StringSchema || hasType(schema, "string")) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return new StringEnumSchema(isNullable(schema), toStringEnumValues(schema.getEnum()));
            }
            return new SimpleStringSchema(isNullable(schema));
        }
        if (schema instanceof IntegerSchema || hasType(schema, "integer")) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return new IntegerEnumSchema(isNullable(schema), toIntegerEnumValues(schema.getEnum()));
            }
            if ("int64".equals(schema.getFormat())) {
                return new SimpleLongSchema(isNullable(schema));
            }
            return new SimpleIntegerSchema(isNullable(schema));
        }
        if (schema instanceof NumberSchema || hasType(schema, "number")) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return new NumberEnumSchema(isNullable(schema), toNumberEnumValues(schema.getEnum()));
            }
            return new SimpleNumberSchema(isNullable(schema));
        }
        if (schema instanceof BooleanSchema || hasType(schema, "boolean")) {
            return new SimpleBooleanSchema(isNullable(schema));
        }

        return new SimpleAnySchema(isNullable(schema));
    }

    private static boolean isNullable(Schema<?> schema) {
        if (hasType(schema, "null")) {
            return true;
        }
        return containsNullOnlyVariant(schema.getAnyOf()) || containsNullOnlyVariant(schema.getOneOf());
    }

    private static boolean hasType(Schema<?> schema, String type) {
        return collectedTypes(schema).contains(type);
    }

    private static boolean usesMixedComposition(Schema<?> schema) {
        int count = 0;
        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            count++;
        }
        if (!nonNullVariants(schema.getAnyOf()).isEmpty()) {
            count++;
        }
        if (!nonNullVariants(schema.getOneOf()).isEmpty()) {
            count++;
        }
        return count > 1;
    }

    private static List<Schema> nonNullVariants(@Nullable List<Schema> variants) {
        if (variants == null || variants.isEmpty()) {
            return List.of();
        }
        return variants.stream()
                .filter(Objects::nonNull)
                .filter(schema -> !isNullOnlySchema(schema))
                .toList();
    }

    private static String discriminatorProperty(Schema<?> schema) {
        if (schema.getDiscriminator() == null) {
            return null;
        }
        return schema.getDiscriminator().getPropertyName();
    }

    private static boolean containsNullOnlyVariant(@Nullable List<Schema> variants) {
        if (variants == null || variants.isEmpty()) {
            return false;
        }
        return variants.stream()
                .filter(Objects::nonNull)
                .anyMatch(Oas31ToSimpleSchemaMapper::isNullOnlySchema);
    }

    private static boolean isNullOnlySchema(Schema<?> schema) {
        Set<String> types = collectedTypes(schema);
        return types.size() == 1 && types.contains("null");
    }

    private static Set<String> collectedTypes(Schema<?> schema) {
        if (schema.getTypes() != null && !schema.getTypes().isEmpty()) {
            return new LinkedHashSet<>(schema.getTypes());
        }
        if (schema.getType() != null) {
            return Set.of(schema.getType());
        }
        return Set.of();
    }

    private static boolean isBinaryStringSchema(@Nullable Schema<?> schema) {
        if (schema == null) {
            return false;
        }
        return (hasType(schema, "string") || schema instanceof StringSchema)
                && "binary".equals(schema.getFormat());
    }

    private static List<String> toStringEnumValues(List<?> values) {
        return values.stream().map(String::valueOf).toList();
    }

    private static List<Integer> toIntegerEnumValues(List<?> values) {
        return values.stream()
                .map(value -> {
                    if (value instanceof Number number) {
                        return number.intValue();
                    }
                    return Integer.parseInt(String.valueOf(value));
                })
                .toList();
    }

    private static List<BigDecimal> toNumberEnumValues(List<?> values) {
        return values.stream()
                .map(value -> {
                    if (value instanceof Number number) {
                        return new BigDecimal(number.toString());
                    }
                    return new BigDecimal(String.valueOf(value));
                })
                .toList();
    }

    @Nullable
    private static List<ResolvedContent> resolveContentSchemas(@Nullable Content content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        List<ResolvedContent> resolvedContents = new ArrayList<>(2);
        MediaType json = content.get(ResponseMediaType.APPLICATION_JSON.value());
        if (json != null && json.getSchema() != null) {
            resolvedContents.add(new ResolvedContent(ResponseMediaType.APPLICATION_JSON, json.getSchema()));
        }
        MediaType octet = content.get(ResponseMediaType.APPLICATION_OCTET_STREAM.value());
        if (octet != null && octet.getSchema() != null) {
            resolvedContents.add(new ResolvedContent(ResponseMediaType.APPLICATION_OCTET_STREAM, octet.getSchema()));
        }
        if (!resolvedContents.isEmpty()) {
            return resolvedContents;
        }
        ResolvedContent fallback = resolveContentSchema(content);
        return fallback == null ? List.of() : List.of(fallback);
    }

    @Nullable
    private static ResolvedContent resolveContentSchema(@Nullable Content content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        MediaType json = content.get(ResponseMediaType.APPLICATION_JSON.value());
        if (json != null && json.getSchema() != null) {
            return new ResolvedContent(ResponseMediaType.APPLICATION_JSON, json.getSchema());
        }

        MediaType octet = content.get(ResponseMediaType.APPLICATION_OCTET_STREAM.value());
        if (octet != null && octet.getSchema() != null) {
            return new ResolvedContent(ResponseMediaType.APPLICATION_OCTET_STREAM, octet.getSchema());
        }

        var firstEntry = content.entrySet().stream().findFirst().orElse(null);
        if (firstEntry == null || firstEntry.getValue() == null || firstEntry.getValue().getSchema() == null) {
            return null;
        }

        return new ResolvedContent(
                ResponseMediaType.from(firstEntry.getKey()),
                firstEntry.getValue().getSchema()
        );
    }

    private record ResolvedContent(ResponseMediaType mediaType, Schema<?> schema) {
    }

    private static OperationName operationName(String path, HttpMethod method, @Nullable String operationId) {
        if (operationId == null || operationId.isBlank()) {
            return OperationName.pathAndMethod(path, method);
        }
        return OperationName.id(operationId);
    }
}
