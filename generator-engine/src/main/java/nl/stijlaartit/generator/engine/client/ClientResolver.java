package nl.stijlaartit.generator.engine.client;

import io.swagger.v3.oas.models.OpenAPI;
import nl.stijlaartit.generator.engine.client.raw.GeneratableOperation;
import nl.stijlaartit.generator.engine.client.raw.NonGeneratableOperation;
import nl.stijlaartit.generator.engine.client.raw.RawOperationResolver;
import nl.stijlaartit.generator.engine.client.raw.RawParameter;
import nl.stijlaartit.generator.engine.client.raw.RequestBodyType;
import nl.stijlaartit.generator.engine.client.raw.ResponseBodyType;
import nl.stijlaartit.generator.engine.domain.ApiFile;
import nl.stijlaartit.generator.engine.domain.OperationModel;
import nl.stijlaartit.generator.engine.domain.OperationName;
import nl.stijlaartit.generator.engine.domain.ParameterLocation;
import nl.stijlaartit.generator.engine.domain.ParameterModel;
import nl.stijlaartit.generator.engine.logger.Logger;
import nl.stijlaartit.generator.engine.model.TypeDescriptor;
import nl.stijlaartit.generator.engine.model.TypeDescriptorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientResolver {

    private final Logger logger;
    private final TypeDescriptorFactory typeDescriptorFactory;

    public ClientResolver(Logger logger, TypeDescriptorFactory typeDescriptorFactory) {
        this.logger = logger;
        this.typeDescriptorFactory = typeDescriptorFactory;
    }


    public List<ApiFile> resolve(OpenAPI openAPI) {
        final var rawOperationResolver = new RawOperationResolver(logger, openAPI);
        final var rawOperations = rawOperationResolver.resolve();

        List<GeneratableOperation> generatableOperations = new ArrayList<>();
        rawOperations.forEach(operation -> {
            switch (operation) {
                case GeneratableOperation generatableOperation -> generatableOperations.add(generatableOperation);
                case NonGeneratableOperation ignored -> {/* TODO Warning */ }
            }
        });

        Set<String> tags = generatableOperations.stream().flatMap(v -> v.tags().stream())
                .collect(Collectors.toSet());

        return tags.stream()
                .map(tag -> apiFileFromTag(tag, generatableOperations, typeDescriptorFactory))
                .toList();
    }

    private ApiFile apiFileFromTag(String tag, List<GeneratableOperation> operations,
                                   TypeDescriptorFactory typeDescriptorFactory) {
        final List<OperationModel> apiOperations = operations
                .stream()
                .filter(operation -> operation.hasTag(tag))
                .map(operation -> rawOperationToOperationModel(operation, typeDescriptorFactory))
                .toList();

        String interfaceName = toPascalCase(tag) + "Api";
        return new ApiFile(interfaceName, apiOperations);
    }

    private OperationModel rawOperationToOperationModel(GeneratableOperation operation,
                                                        TypeDescriptorFactory typeDescriptorFactory) {
        String operationId = operation.operationId();
        final OperationName rawName = (operationId == null || operationId.isBlank()) ?
                OperationName.pathAndMethod(operation.path(), operation.method())
                : OperationName.id(operation.operationId());

        TypeDescriptor requestBodyType = switch (operation.requestBodyType()) {
            case RequestBodyType.None ignored -> null;
            case RequestBodyType.Resource ignored -> TypeDescriptor.simple("org.springframework.core.io.Resource");
            case RequestBodyType.Unknown ignored -> TypeDescriptor.simple("java.lang.Object");
            case RequestBodyType.Typed typed -> typeDescriptorFactory.build(typed.schema());
        };

        TypeDescriptor responseType = switch (operation.responseBodyType()) {
            case ResponseBodyType.None ignored -> null;
            // TODO Missing Resource? or Unknown?
            case ResponseBodyType.SchemaType typed -> typeDescriptorFactory.build(typed.schema());
        };

        return new OperationModel(
                rawName,
                operation.method(),
                operation.path(),
                operation.parameters().stream().map(param -> rawParameterToParameterModel(param, typeDescriptorFactory)).toList(),
                requestBodyType,
                responseType,
                operation.deprecated()
        );
    }

    private ParameterModel rawParameterToParameterModel(RawParameter parameter,
                                                        TypeDescriptorFactory typeDescriptorFactory) {
        return new ParameterModel(
                parameter.name(),
                switch (parameter.location()) {
                    case PATH -> ParameterLocation.PATH;
                    case QUERY -> ParameterLocation.QUERY;
                    case HEADER -> ParameterLocation.HEADER;
                },
                typeDescriptorFactory.build(parameter.schema()),
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
