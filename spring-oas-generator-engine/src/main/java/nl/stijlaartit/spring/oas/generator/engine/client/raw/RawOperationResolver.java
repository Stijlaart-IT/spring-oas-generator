package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleBinarySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleReponse;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOperation;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
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
            operations.add(buildGeneratableOperation(operation));
        }
        return List.copyOf(operations);
    }

    private GeneratableOperation buildGeneratableOperation(SimplifiedOperation operation) {
        List<String> tags = operation.tags().isEmpty()
                ? List.of("default")
                : operation.tags().stream().sorted().toList();
        List<RawParameter> parameters = resolveParameters(operation.path(), operation.params());
        String operationId = operation.operationId();

        final var operationName = operation.operationId() == null
                ? OperationName.pathAndMethod(operation.path(), operation.method())
                : OperationName.id(operation.operationId());

        RequestBodyType requestBodyType = operation.requestBody() == null
                ? new RequestBodyType.None()
                : operation.requestBody() instanceof SimpleBinarySchema
                ? new RequestBodyType.Resource()
                : new RequestBodyType.Typed(operation.requestBody());
        ResponseBodyType responseBodyType = resolveResponseType(operationName, operation.responses());

        return new GeneratableOperation(
                operation.path(),
                operation.method(),
                operationId,
                parameters,
                requestBodyType,
                responseBodyType,
                tags,
                false
        );
    }

    @NonNull
    private ResponseBodyType resolveResponseType(OperationName operationName, List<SimpleReponse> responses) {
        SimpleReponse responseWithBody = null;
        String responseWithBodyCode = null;
        Optional<SimpleSchema> responseWithBodySchema = null;

        List<String> successCodes = new ArrayList<>();
        for (SimpleReponse response : responses) {
            String code = response.status();
            if (code == null || !code.startsWith("2")) {
                continue;
            }
            successCodes.add(code);
            Optional<SimpleSchema> statusContentSchema = Optional.of(response.schema());
            if (responseWithBody != null) {
                if (statusContentSchema.equals(responseWithBodySchema)) {
                    logger.warn("Operation '" + operationName.format() + "' defines multiple 2xx responses with same body. Using first response body.");
                    continue;
                }
                throw new IllegalArgumentException("Multiple 2xx responses with body defined for operation '"
                        + operationName.format() + "': " + responseWithBodyCode + " and " + code);
            }
            responseWithBody = response;
            responseWithBodyCode = code;
            responseWithBodySchema = statusContentSchema;
        }

        if (responseWithBody != null && successCodes.size() > 1) {
            logger.warn("Operation '" + operationName.format()
                    + "' defines multiple 2xx responses. Using response " + responseWithBodyCode + ".");
            return new ResponseBodyType.SchemaType(responseWithBody.schema());
        }

        if (responseWithBody != null) {
            return new ResponseBodyType.SchemaType(responseWithBody.schema());
        }
        return new ResponseBodyType.None();
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
