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
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
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
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedRequest;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.math.BigDecimal;

public final class Oas30ToSimpleSchemaMapper {
    private final Logger logger;

    public Oas30ToSimpleSchemaMapper() {
        this(Logger.noOp());
    }

    public Oas30ToSimpleSchemaMapper(Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    public SimplifiedOas resolve(OpenAPI openAPI) {
        Objects.requireNonNull(openAPI);

        final Map<String, SimpleSchema> componentSchemas = resolveComponentSchemas(openAPI);
        final Map<String, SimpleSchema> componentResponses = resolveComponentResponseSchemas(openAPI, componentSchemas);
        final Map<String, SimpleSchema> componentParameters = resolveComponentParameters(openAPI);
        final Map<String, List<SimpleParam>> pathParams = new LinkedHashMap<>();
        final List<SimplifiedOperation> operations = resolveOperations(openAPI, pathParams);

        return new SimplifiedOas(componentSchemas, componentResponses, componentParameters, operations, pathParams);
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

    private Map<String, SimpleSchema> resolveComponentResponseSchemas(OpenAPI openAPI, Map<String, SimpleSchema> componentSchemas) {
        final Map<String, SimpleSchema> mapped = new LinkedHashMap<>();
        if (openAPI.getComponents() == null || openAPI.getComponents().getResponses() == null) {
            return mapped;
        }

        for (var entry : openAPI.getComponents().getResponses().entrySet()) {
            if (componentSchemas.containsKey(entry.getKey())) {
                continue;
            }
            final ApiResponse resolved = resolveResponseRef(openAPI, entry.getValue());
            if (resolved == null) {
                continue;
            }
            final List<ResolvedContent> resolvedContents = resolveContentSchemas(resolved.getContent());
            if (resolvedContents.isEmpty()) {
                continue;
            }
            final ResolvedContent firstContent = resolvedContents.getFirst();
            final var path = SchemaPath.forRoot(PathRoot.componentSchema(entry.getKey()));
            final SimpleSchema schema = firstContent.mediaType() == ResponseMediaType.APPLICATION_OCTET_STREAM
                    && isBinaryStringSchema(firstContent.schema())
                    ? new SimpleBinarySchema(isNullable(firstContent.schema()))
                    : mapSchema(firstContent.schema(), path);
            mapped.put(entry.getKey(), schema);
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
        final List<SimpleReponse> responses = mapResponses(openAPI, operationName, operation.getResponses());
        final SimplifiedRequest request = mapRequestBody(openAPI, operationName, operation.getRequestBody());

        operations.add(new SimplifiedOperation(path, method, operationId, tags, params, responses, request));
    }

    private List<SimpleReponse> mapResponses(OpenAPI openAPI, OperationName operationName, @Nullable ApiResponses responses) {
        if (responses == null) {
            return List.of();
        }

        final List<SimpleReponse> mapped = new ArrayList<>();
        for (var entry : responses.entrySet()) {
            final String status = entry.getKey();
            final ApiResponse response = resolveResponseRef(openAPI, entry.getValue());
            if (response == null) {
                continue;
            }
            final List<ResolvedContent> resolvedContents = resolveContentSchemas(response.getContent());
            final SchemaRef responseRef = entry.getValue() != null && entry.getValue().get$ref() != null
                    ? SchemaRef.parseFromRefValue(entry.getValue().get$ref())
                    : null;
            for (ResolvedContent resolvedContent : resolvedContents) {
                final SimpleSchema schema;
                if (responseRef != null) {
                    if (!"responses".equals(responseRef.type())) {
                        throw new IllegalArgumentException("Only component response refs are supported: " + entry.getValue().get$ref());
                    }
                    schema = new RefSchema(false, new SchemaRef("schemas", responseRef.name()));
                } else {
                    final SchemaPath path = SchemaPath.forRoot(PathRoot.responseBody(operationName, status));
                    schema = resolvedContent.mediaType() == ResponseMediaType.APPLICATION_OCTET_STREAM
                            && isBinaryStringSchema(resolvedContent.schema())
                            ? new SimpleBinarySchema(isNullable(resolvedContent.schema()))
                            : mapSchema(resolvedContent.schema(), path);
                }
                mapped.add(new SimpleReponse(status, schema, resolvedContent.mediaType()));
            }
        }
        return mapped;
    }

    @Nullable
    private SimplifiedRequest mapRequestBody(OpenAPI openAPI, OperationName operationName, @Nullable RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }
        final RequestBody resolved = resolveRequestBodyRef(openAPI, requestBody);
        final Content content = resolved.getContent();
        if (content == null || content.isEmpty()) {
            logger.warn("Operation '" + operationName.format()
                    + "' request body has no media types with schemas. Request body ignored.");
            return null;
        }

        final MediaType json = content.get(ResponseMediaType.APPLICATION_JSON.value());
        final boolean hasJson = json != null && json.getSchema() != null;

        String binaryMediaType = null;
        int binaryMediaTypeCount = 0;
        for (var entry : content.entrySet()) {
            MediaType candidate = entry.getValue();
            if (candidate == null || candidate.getSchema() == null) {
                continue;
            }
            if (!isBinaryCandidateRequestMediaType(entry.getKey())) {
                continue;
            }
            if (!isBinaryStringSchema(candidate.getSchema())) {
                continue;
            }
            binaryMediaTypeCount++;
            if (binaryMediaType == null) {
                binaryMediaType = entry.getKey();
            }
        }

        if (hasJson && binaryMediaTypeCount == 0) {
            final var path = SchemaPath.forRoot(PathRoot.requestBody(operationName));
            return new SimplifiedRequest.Json(mapSchema(json.getSchema(), path), ResponseMediaType.APPLICATION_JSON.value());
        }
        if (!hasJson && binaryMediaTypeCount == 1) {
            return new SimplifiedRequest.Binary(binaryMediaType);
        }

        logger.warn("Operation '" + operationName.format()
                + "' request body media types " + requestBodyMediaTypes(content)
                + " are not supported. Request body ignored.");
        return null;
    }

    private RequestBody resolveRequestBodyRef(OpenAPI openAPI, RequestBody requestBody) {
        if (requestBody.get$ref() == null) {
            return requestBody;
        }

        final Map<String, RequestBody> componentRequestBodies = openAPI.getComponents() == null
                || openAPI.getComponents().getRequestBodies() == null
                ? Map.of()
                : openAPI.getComponents().getRequestBodies();

        RequestBody current = requestBody;
        Set<SchemaRef> visitedRefs = new HashSet<>();
        while (current.get$ref() != null) {
            final SchemaRef ref = SchemaRef.parseFromRefValue(current.get$ref());
            if (!"requestBodies".equals(ref.type())) {
                throw new IllegalArgumentException("Only component request body refs are supported: " + current.get$ref());
            }
            if (!visitedRefs.add(ref)) {
                throw new IllegalArgumentException("Circular request body reference detected: " + ref.name());
            }
            current = componentRequestBodies.get(ref.name());
            if (current == null) {
                throw new IllegalArgumentException("Request body reference not found: " + ref.name());
            }
        }
        return current;
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
        if (parameter == null || parameter.get$ref() == null) {
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

    private ApiResponse resolveResponseRef(OpenAPI openAPI, ApiResponse response) {
        if (response == null || response.get$ref() == null) {
            return response;
        }

        final Map<String, ApiResponse> componentResponses = openAPI.getComponents() == null
                || openAPI.getComponents().getResponses() == null
                ? Map.of()
                : openAPI.getComponents().getResponses();

        ApiResponse current = response;
        Set<SchemaRef> visitedRefs = new HashSet<>();
        while (current.get$ref() != null) {
            final SchemaRef ref = SchemaRef.parseFromRefValue(current.get$ref());
            if (!"responses".equals(ref.type())) {
                throw new IllegalArgumentException("Only component response refs are supported: " + current.get$ref());
            }
            if (!visitedRefs.add(ref)) {
                throw new IllegalArgumentException("Circular response reference detected: " + ref.name());
            }
            current = componentResponses.get(ref.name());
            if (current == null) {
                throw new IllegalArgumentException("Response reference not found: " + ref.name());
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

        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            List<SimpleSchema> variants = new ArrayList<>();
            for (int i = 0; i < schema.getOneOf().size(); i++) {
                variants.add(mapSchema(schema.getOneOf().get(i), path.variant("oneOf", i)));
            }
            return new UnionSchema(
                    isNullable(schema),
                    variants,
                    discriminatorProperty(schema)
            );
        }

        if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            List<SimpleSchema> components = new ArrayList<>();
            for (int i = 0; i < schema.getAnyOf().size(); i++) {
                components.add(mapSchema(schema.getAnyOf().get(i), path.variant("anyOf", i)));
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

        if (schema instanceof ArraySchema || "array".equals(schema.getType())) {
            final SimpleSchema itemSchema = schema.getItems() == null
                    ? new SimpleAnySchema(false)
                    : mapSchema(schema.getItems(), path.items());
            return new SimpleArraySchema(isNullable(schema), itemSchema);
        }

        if (schema instanceof ObjectSchema || schema.getProperties() != null || schema.getAdditionalProperties() != null) {
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

        if (schema instanceof StringSchema || "string".equals(schema.getType())) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return new StringEnumSchema(isNullable(schema), toStringEnumValues(schema.getEnum()));
            }
            return new SimpleStringSchema(isNullable(schema));
        }
        if (schema instanceof IntegerSchema || "integer".equals(schema.getType())) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return new IntegerEnumSchema(isNullable(schema), toIntegerEnumValues(schema.getEnum()));
            }
            if ("int64".equals(schema.getFormat())) {
                return new SimpleLongSchema(isNullable(schema));
            }
            return new SimpleIntegerSchema(isNullable(schema));
        }
        if (schema instanceof NumberSchema || "number".equals(schema.getType())) {
            if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
                return new NumberEnumSchema(isNullable(schema), toNumberEnumValues(schema.getEnum()));
            }
            return new SimpleNumberSchema(isNullable(schema));
        }
        if (schema instanceof BooleanSchema || "boolean".equals(schema.getType())) {
            return new SimpleBooleanSchema(isNullable(schema));
        }

        return new SimpleAnySchema(isNullable(schema));
    }

    private static String discriminatorProperty(Schema<?> schema) {
        if (schema.getDiscriminator() == null) {
            return null;
        }
        return schema.getDiscriminator().getPropertyName();
    }

    private static boolean isNullable(Schema<?> schema) {
        return Boolean.TRUE.equals(schema.getNullable());
    }

    private static boolean usesMixedComposition(Schema<?> schema) {
        int count = 0;
        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            count++;
        }
        if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
            count++;
        }
        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            count++;
        }
        return count > 1;
    }

    private static boolean isBinaryStringSchema(@Nullable Schema<?> schema) {
        if (schema == null) {
            return false;
        }
        return ("string".equals(schema.getType()) || schema instanceof StringSchema)
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

    private static List<String> requestBodyMediaTypes(Content content) {
        List<String> mediaTypes = new ArrayList<>();
        for (var entry : content.entrySet()) {
            MediaType mediaType = entry.getValue();
            mediaTypes.add(entry.getKey() + (mediaType != null && mediaType.getSchema() != null ? "" : " (no schema)"));
        }
        return mediaTypes;
    }

    private static boolean isBinaryCandidateRequestMediaType(String mediaType) {
        return ResponseMediaType.APPLICATION_OCTET_STREAM.value().equals(mediaType);
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
