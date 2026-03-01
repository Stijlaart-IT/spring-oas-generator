package nl.stijlaartit.spring.oas.generator.engine.client;

import nl.stijlaartit.spring.oas.generator.domain.file.ApiFile;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiHttpMethod;
import nl.stijlaartit.spring.oas.generator.domain.file.ApiOperation;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaMethodName;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.ParameterLocation;
import nl.stijlaartit.spring.oas.generator.domain.file.ParameterModel;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.engine.client.raw.GeneratableOperation;
import nl.stijlaartit.spring.oas.generator.engine.client.raw.NonGeneratableOperation;
import nl.stijlaartit.spring.oas.generator.engine.client.raw.RawOperationResolver;
import nl.stijlaartit.spring.oas.generator.engine.client.raw.RawParameter;
import nl.stijlaartit.spring.oas.generator.engine.client.raw.RequestBodyType;
import nl.stijlaartit.spring.oas.generator.engine.client.raw.ResponseBodyType;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import nl.stijlaartit.spring.oas.generator.engine.model.TypeInfoResolver;
import nl.stijlaartit.spring.oas.generator.engine.naming.NamingUtil;
import nl.stijlaartit.spring.oas.generator.engine.naming.OperationIdNaming;
import nl.stijlaartit.spring.oas.generator.serialization.JavaIdentifierUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientResolver {

    public static final TypeDescriptor SPRING_RESOURCE_TYPE_DESCRIPTOR = TypeDescriptor.qualified("org.springframework.core.io", new JavaTypeName.Generated("Resource"));
    private final Logger logger;
    private final TypeInfoResolver typeInfoResolver;

    public ClientResolver(Logger logger, TypeInfoResolver typeInfoResolver) {
        this.logger = logger;
        this.typeInfoResolver = typeInfoResolver;
    }


    public List<ApiFile> resolve(SimplifiedOas simplifiedOas) {
        final var rawOperationResolver = new RawOperationResolver(logger, simplifiedOas);
        final var rawOperations = rawOperationResolver.resolve();

        List<GeneratableOperation> generatableOperations = new ArrayList<>();
        rawOperations.forEach(operation -> {
            switch (operation) {
                case GeneratableOperation generatableOperation -> generatableOperations.add(generatableOperation);
                case NonGeneratableOperation ignoredOperation -> {
                    String id = ignoredOperation.operationId() == null ?
                            ignoredOperation.method().name() + " " + ignoredOperation.path()
                            : ignoredOperation.operationId();
                    logger.warn("Ignoring operation [" + id + "] due to: " + ignoredOperation.message());
                }
            }
        });

        Set<String> tags = generatableOperations.stream().flatMap(v -> v.tags().stream())
                .collect(Collectors.toSet());

        return tags.stream()
                .map(tag -> apiFileFromTag(tag, generatableOperations))
                .toList();
    }

    private ApiFile apiFileFromTag(String tag, List<GeneratableOperation> operations) {
        final List<ApiOperation> apiOperations = operations
                .stream()
                .filter(operation -> operation.hasTag(tag))
                .map(this::rawOperationToOperationModel)
                .toList();

        String interfaceName = toPascalCase(removeInvalidTypeNameCharacters(tag)) + "Api";
        return new ApiFile(interfaceName, apiOperations);
    }

    private ApiOperation rawOperationToOperationModel(GeneratableOperation operation) {
        String operationId = operation.operationId();
        final OperationName rawName = (operationId == null || operationId.isBlank()) ?
                OperationName.pathAndMethod(operation.path(), operation.method())
                : OperationName.id(operation.operationId());

        TypeDescriptor requestBodyType = switch (operation.requestBodyType()) {
            case RequestBodyType.None ignored -> null;
            case RequestBodyType.Resource ignored -> SPRING_RESOURCE_TYPE_DESCRIPTOR;
            case RequestBodyType.Unknown ignored ->
                    TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"));
            case RequestBodyType.Typed typed -> typeInfoResolver.get(typed.schema()).typeDescriptor();
        };

        TypeDescriptor responseType = switch (operation.responseBodyType()) {
            case ResponseBodyType.None ignored -> null;
            // TODO Missing Resource? or Unknown?
            case ResponseBodyType.SchemaType typed -> typeInfoResolver.get(typed.schema()).typeDescriptor();
        };

        return new ApiOperation(
                methodNameFromOperationName(rawName),
                toApiHttpMethod(operation.method()),
                operation.path(),
                operation.parameters().stream().map(this::rawParameterToParameterModel).toList(),
                requestBodyType,
                responseType,
                operation.deprecated()
        );
    }

    private static ApiHttpMethod toApiHttpMethod(nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod method) {
        return switch (method) {
            case GET -> ApiHttpMethod.GET;
            case POST -> ApiHttpMethod.POST;
            case PUT -> ApiHttpMethod.PUT;
            case DELETE -> ApiHttpMethod.DELETE;
            case PATCH -> ApiHttpMethod.PATCH;
        };
    }

    private JavaMethodName methodNameFromOperationName(OperationName name) {
        return new JavaMethodName(JavaIdentifierUtils.sanitize(NamingUtil.toCamelCase(switch (name) {
            case OperationName.Id id -> id.value();
            case OperationName.PathAndMethod pathAndMethod ->
                    OperationIdNaming.fallbackOperationId(pathAndMethod.method(), pathAndMethod.path());
        })));
    }

    private ParameterModel rawParameterToParameterModel(RawParameter parameter) {
        return new ParameterModel(
                parameter.name(),
                switch (parameter.location()) {
                    case PATH -> ParameterLocation.PATH;
                    case QUERY -> ParameterLocation.QUERY;
                    case HEADER -> ParameterLocation.HEADER;
                },
                typeInfoResolver.get(parameter.schema()).typeDescriptor(),
                parameter.required()
        );
    }


    static String toPascalCase(String input) {
        if (input.isEmpty()) {
            return input;
        }
        String camel = toCamelCase(input);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    private static String removeInvalidTypeNameCharacters(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                if (c == ' ' || c == '_' || c == '-' || c == '.') {
                    capitalizeNext = true;
                }
                continue;
            }
            if (result.isEmpty()) {
                if (!Character.isJavaIdentifierStart(c)) {
                    continue;
                }
                result.append(Character.toLowerCase(c));
                capitalizeNext = false;
                continue;
            }
            if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    static String toCamelCase(String input) {
        if (input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_' || c == '-' || c == ' ') {
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
}
