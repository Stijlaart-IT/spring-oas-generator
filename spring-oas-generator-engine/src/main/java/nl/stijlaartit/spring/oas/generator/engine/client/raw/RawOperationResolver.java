package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ResponseMediaType;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleReponse;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOperation;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedRequest;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.naming.OperationIdNaming;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RawOperationResolver {

    private final Logger logger;
    private final SimplifiedOas simplifiedOas;

    public RawOperationResolver(Logger logger, SimplifiedOas simplifiedOas) {
        this.logger = logger;
        this.simplifiedOas = simplifiedOas;
    }

    public List<RawOperation> resolve() {
        List<RawOperation> operations = new ArrayList<>();
        for (SimplifiedOperation operation : simplifiedOas.operations()) {
            operations.addAll(buildGeneratableOperations(operation));
        }
        return List.copyOf(operations);
    }

    private List<GeneratableOperation> buildGeneratableOperations(SimplifiedOperation operation) {
        List<String> tags = operation.tags().isEmpty()
                ? List.of("default")
                : operation.tags().stream().toList();
        List<RawParameter> parameters = resolveParameters(operation.path(), operation.params());
        String operationId = normalizeOperationId(operation.operationId());

        final var operationName = operation.operationId() == null
                ? OperationName.pathAndMethod(operation.path(), operation.method())
                : OperationName.id(operation.operationId());

        final RequestBodyType requestBodyType;
        if (operation.request() == null) {
            requestBodyType = new RequestBodyType.None();
        } else if (operation.request() instanceof SimplifiedRequest.Binary binary) {
            requestBodyType = new RequestBodyType.Resource(binary.mediaType());
        } else if (operation.request() instanceof SimplifiedRequest.Json json) {
            requestBodyType = new RequestBodyType.Typed(json.schema(), json.mediaType());
        } else {
            throw new IllegalStateException("Unhandled request variant: " + operation.request().getClass().getSimpleName());
        }
        ResponseBodyType jsonResponseType = resolveResponseType(
                operationName,
                operation.responses(),
                ResponseMediaType.APPLICATION_JSON
        );
        ResponseBodyType octetResponseType = resolveResponseType(
                operationName,
                operation.responses(),
                ResponseMediaType.APPLICATION_OCTET_STREAM
        );

        boolean hasJsonResponse = jsonResponseType instanceof ResponseBodyType.SchemaType;
        boolean hasBinaryResponse = octetResponseType instanceof ResponseBodyType.SchemaType;

        if (hasJsonResponse && hasBinaryResponse) {
            String binaryOperationId = binaryOperationId(operation);
            return List.of(
                    new GeneratableOperation(
                            operation.path(),
                            operation.method(),
                            operationId,
                            parameters,
                            requestBodyType,
                            jsonResponseType,
                            tags,
                            false
                    ),
                    new GeneratableOperation(
                            operation.path(),
                            operation.method(),
                            binaryOperationId,
                            parameters,
                            requestBodyType,
                            octetResponseType,
                            tags,
                            false
                    )
            );
        }

        ResponseBodyType responseBodyType = hasJsonResponse
                ? jsonResponseType
                : hasBinaryResponse
                ? octetResponseType
                : resolveResponseType(operationName, operation.responses(), ResponseMediaType.UNKNOWN);

        return List.of(new GeneratableOperation(
                operation.path(),
                operation.method(),
                operationId,
                parameters,
                requestBodyType,
                responseBodyType,
                tags,
                false
        ));
    }

    @NonNull
    private ResponseBodyType resolveResponseType(OperationName operationName,
                                                 List<SimpleReponse> responses,
                                                 ResponseMediaType mediaType) {
        SimpleReponse responseWithBody = null;
        String responseWithBodyCode = null;
        Optional<SimpleSchema> responseWithBodySchema = null;

        List<String> successCodes = new ArrayList<>();
        for (SimpleReponse response : responses) {
            String code = response.status();
            if (code == null || !code.startsWith("2")) {
                continue;
            }
            if (mediaType != ResponseMediaType.UNKNOWN && response.mediaType() != mediaType) {
                continue;
            }
            successCodes.add(code);
            Optional<SimpleSchema> statusContentSchema = Optional.of(response.schema());
            if (responseWithBody != null) {
                if (statusContentSchema.equals(responseWithBodySchema)) {
                    logger.warn("Operation '" + operationName.format()
                            + "' defines multiple 2xx responses with same body"
                            + mediaTypeMessageSuffix(mediaType)
                            + ". Using first response body.");
                    continue;
                }
                throw new IllegalArgumentException("Multiple 2xx responses with body defined for operation '"
                        + operationName.format() + "'" + mediaTypeMessageSuffix(mediaType)
                        + ": " + responseWithBodyCode + " and " + code);
            }
            responseWithBody = response;
            responseWithBodyCode = code;
            responseWithBodySchema = statusContentSchema;
        }

        if (responseWithBody != null && successCodes.size() > 1) {
            logger.warn("Operation '" + operationName.format()
                    + "' defines multiple 2xx responses" + mediaTypeMessageSuffix(mediaType)
                    + ". Using response " + responseWithBodyCode + ".");
            return new ResponseBodyType.SchemaType(responseWithBody.schema(), responseWithBody.mediaType());
        }

        if (responseWithBody != null) {
            return new ResponseBodyType.SchemaType(responseWithBody.schema(), responseWithBody.mediaType());
        }
        return new ResponseBodyType.None();
    }

    private static String mediaTypeMessageSuffix(ResponseMediaType mediaType) {
        if (!mediaType.isKnown()) {
            return "";
        }
        return " for media type " + mediaType.value();
    }

    private static String normalizeOperationId(String operationId) {
        return (operationId == null || operationId.isBlank()) ? null : operationId;
    }

    private static String binaryOperationId(SimplifiedOperation operation) {
        String base = normalizeOperationId(operation.operationId());
        if (base == null) {
            base = OperationIdNaming.fallbackOperationId(operation.method(), operation.path());
        }
        return base + "Binary";
    }

    private List<RawParameter> resolveParameters(String path, List<SimpleParam> operationParameters) {
        Map<String, SimpleParam> merged = new LinkedHashMap<>();
        for (SimpleParam pathParam : simplifiedOas.pathParams().getOrDefault(path, List.of())) {
            merged.put(pathParam.in().name() + ":" + pathParam.name(), pathParam);
        }
        for (SimpleParam opParam : operationParameters) {
            merged.put(opParam.in().name() + ":" + opParam.name(), opParam);
        }

        return merged.values().stream().map(this::toRawParameter).toList();
    }

    private RawParameter toRawParameter(SimpleParam param) {
        RawParameter.ParameterLocation location = switch (param.in()) {
            case Path -> RawParameter.ParameterLocation.PATH;
            case Query -> RawParameter.ParameterLocation.QUERY;
            case Header -> RawParameter.ParameterLocation.HEADER;
        };
        return new RawParameter(param.name(), location, param.schema(), param.required());
    }
}
