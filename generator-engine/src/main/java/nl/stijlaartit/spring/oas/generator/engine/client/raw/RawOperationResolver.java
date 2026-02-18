package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RawOperationResolver {

    private final Logger logger;
    private final OpenAPI openAPI;

    public RawOperationResolver(Logger logger, OpenAPI openAPI) {
        this.logger = logger;
        this.openAPI = openAPI;
    }

    public List<RawOperation> resolve() {
        List<RawOperation> operations = new ArrayList<>();

        if (openAPI.getPaths() != null) {
            for (var pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                operations.addAll(resolvePathOperations(path, pathItem));
            }
        }


        return List.copyOf(operations);
    }

    @NonNull
    private List<RawOperation> resolvePathOperations(String path, PathItem pathItem) {
        return pathItem.readOperationsMap()
                .entrySet()
                .stream()
                .map(entry -> buildRawOperation(path, entry.getKey(), entry.getValue()))
                .toList();
    }

    private RawOperation buildRawOperation(String path, PathItem.HttpMethod key, Operation value) {
        return switch (key) {
            case HEAD -> new NonGeneratableOperation(path, key, value.getOperationId(), "Method HEAD not supported");
            case OPTIONS ->
                    new NonGeneratableOperation(path, key, value.getOperationId(), "Method OPTIONS not supported");
            case TRACE -> new NonGeneratableOperation(path, key, value.getOperationId(), "Method TRACE not supported");
            case GET -> buildGeneratableOperation(path, HttpMethod.GET, value);
            case POST -> buildGeneratableOperation(path, HttpMethod.POST, value);
            case PUT -> buildGeneratableOperation(path, HttpMethod.PUT, value);
            case PATCH -> buildGeneratableOperation(path, HttpMethod.PATCH, value);
            case DELETE -> buildGeneratableOperation(path, HttpMethod.DELETE, value);
        };
    }

    private GeneratableOperation buildGeneratableOperation(String path, HttpMethod method, Operation operation) {
        List<String> tags = (operation.getTags() != null && !operation.getTags().isEmpty())
                ? operation.getTags()
                : List.of("default");

        List<RawParameter> parameters = resolveParameters(operation.getParameters());
        String operationId = operation.getOperationId();

        RequestBodyType requestBodyType = resolveRequestBodyType(operation.getRequestBody());
        ResponseBodyType responseBodyType = resolveResponseType(operationId, operation.getResponses());

        boolean deprecated = operation.getDeprecated() != null && operation.getDeprecated();

        return new GeneratableOperation(
                path,
                method,
                operationId,
                parameters,
                requestBodyType,
                responseBodyType,
                tags,
                deprecated
        );
    }

    @NonNull
    private ResponseBodyType resolveResponseType(String operationId, @Nullable ApiResponses responses) {
        if (responses == null) {
            return new ResponseBodyType.None();
        }

        ApiResponse successResponse = findSuccessResponse(operationId, responses);
        if (successResponse == null) {
            return new ResponseBodyType.None();
        }

        Content content = successResponse.getContent();
        if (content == null) {
            return new ResponseBodyType.None();
        }

        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = content.values().iterator().next();
        }
        if (mediaType == null || mediaType.getSchema() == null) {
            return new ResponseBodyType.None();
        }
        return new ResponseBodyType.SchemaType(mediaType.getSchema());
    }

    @Nullable
    private ApiResponse findSuccessResponse(String operationId, ApiResponses responses) {
        ApiResponse responseWithBody = null;
        String responseWithBodyCode = null;
        List<String> successCodes = new ArrayList<>();

        for (String code : responses.keySet()) {
            if (code == null || !code.startsWith("2")) {
                continue;
            }
            successCodes.add(code);
            ApiResponse candidate = responses.get(code);
            Content content = candidate != null ? candidate.getContent() : null;
            if (content == null || content.isEmpty()) {
                continue;
            }
            if (responseWithBody != null) {
                throw new IllegalArgumentException("Multiple 2xx responses with body defined for operation '"
                        + operationId + "': " + responseWithBodyCode + " and " + code);
            }
            responseWithBody = candidate;
            responseWithBodyCode = code;
        }

        if (responseWithBody != null && successCodes.size() > 1) {
            logger.warn("[warn] Operation '" + operationId
                    + "' defines multiple 2xx responses. Using response " + responseWithBodyCode + ".");
        }

        return responseWithBody;
    }

    private RequestBodyType resolveRequestBodyType(@Nullable RequestBody requestBody) {
        if (requestBody == null || requestBody.getContent() == null) {
            return new RequestBodyType.None();
        }

        Content content = requestBody.getContent();
        MediaType mediaType = content.get("application/json");
        if (mediaType == null && content.get("application/octet-stream") != null) {
            return new RequestBodyType.Resource();
        }
        if (mediaType == null) {
            mediaType = content.values().iterator().next();
        }
        if (mediaType == null || mediaType.getSchema() == null) {
            return new RequestBodyType.Unknown();
        }

        return new RequestBodyType.Typed(mediaType.getSchema());
    }


    private List<RawParameter> resolveParameters(@Nullable List<Parameter> parameters) {
        if (parameters == null) {
            return List.of();
        }
        return parameters.stream().map(this::resolveParameter).toList();
    }

    private RawParameter resolveParameter(Parameter param) {
        Parameter resolved = resolveParameterRef(param);
        String in = resolved.getIn();
        if (resolved.getName() == null) {
            throw new IllegalArgumentException("Parameter has no name.");
        }
        if (in == null) {
            throw new IllegalArgumentException("Parameter '" + resolved.getName() + "' has no 'in'.");
        }

        RawParameter.ParameterLocation location = switch (in) {
            case "path" -> RawParameter.ParameterLocation.PATH;
            case "query" -> RawParameter.ParameterLocation.QUERY;
            case "header" -> RawParameter.ParameterLocation.HEADER;
            default -> throw new IllegalArgumentException("Unsupported parameter location: " + in);
        };

        boolean required = resolved.getRequired() != null && resolved.getRequired();
        return new RawParameter(resolved.getName(), location, resolved.getSchema(), required);
    }

    private Parameter resolveParameterRef(Parameter parameter) {
        Map<String, Parameter> componentParameters = Map.of();
        if (openAPI.getComponents() != null && openAPI.getComponents().getParameters() != null) {
            componentParameters = openAPI.getComponents().getParameters();
        }

        if (parameter == null || parameter.get$ref() == null) {
            return parameter;
        }

        Parameter current = parameter;
        Set<SchemaRef> visitedRefs = new HashSet<>();
        while (current.get$ref() != null) {
            String ref = current.get$ref();
            final var componentSchemaRef = SchemaRef.parseFromRefValue(ref);
            if (!visitedRefs.add(componentSchemaRef)) {
                throw new IllegalArgumentException("Circular parameter reference detected: " + componentSchemaRef.name());
            }
            current = componentParameters.get(componentSchemaRef.name());
            if (current == null) {
                throw new IllegalArgumentException("Parameter reference not found: " + ref);
            }
        }
        return current;
    }
}
