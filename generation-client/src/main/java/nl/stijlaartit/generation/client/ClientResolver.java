package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.model.TypeDescriptor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClientResolver {

    private Map<String, Schema> componentSchemas = Map.of();

    public List<ClientDescriptor> resolve(OpenAPI openAPI) {
        componentSchemas = Map.of();
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            componentSchemas = openAPI.getComponents().getSchemas();
        }
        Map<String, List<OperationDescriptor>> operationsByTag = new LinkedHashMap<>();

        if (openAPI.getPaths() != null) {
            for (var pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                resolvePathItem(path, pathItem, operationsByTag);
            }
        }

        List<ClientDescriptor> clients = new ArrayList<>();
        for (var entry : operationsByTag.entrySet()) {
            String interfaceName = toPascalCase(entry.getKey()) + "Api";
            clients.add(new ClientDescriptor(interfaceName, entry.getValue()));
        }
        return clients;
    }

    private void resolvePathItem(String path, PathItem pathItem,
                                  Map<String, List<OperationDescriptor>> operationsByTag) {
        addOperation(path, OperationDescriptor.HttpMethod.GET, pathItem.getGet(), operationsByTag);
        addOperation(path, OperationDescriptor.HttpMethod.POST, pathItem.getPost(), operationsByTag);
        addOperation(path, OperationDescriptor.HttpMethod.PUT, pathItem.getPut(), operationsByTag);
        addOperation(path, OperationDescriptor.HttpMethod.DELETE, pathItem.getDelete(), operationsByTag);
        addOperation(path, OperationDescriptor.HttpMethod.PATCH, pathItem.getPatch(), operationsByTag);
    }

    private void addOperation(String path, OperationDescriptor.HttpMethod method,
                               Operation operation,
                               Map<String, List<OperationDescriptor>> operationsByTag) {
        if (operation == null) {
            return;
        }

        String tag = (operation.getTags() != null && !operation.getTags().isEmpty())
                ? operation.getTags().get(0)
                : "default";

        List<ParameterDescriptor> parameters = resolveParameters(operation.getParameters());
        TypeDescriptor requestBody = resolveRequestBody(operation.getRequestBody());
        TypeDescriptor responseType = resolveResponseType(operation.getResponses());

        OperationDescriptor descriptor = new OperationDescriptor(
                operation.getOperationId(), method, path, parameters, requestBody, responseType
        );

        operationsByTag.computeIfAbsent(tag, k -> new ArrayList<>()).add(descriptor);
    }

    private List<ParameterDescriptor> resolveParameters(List<Parameter> parameters) {
        if (parameters == null) {
            return List.of();
        }

        List<ParameterDescriptor> result = new ArrayList<>();
        for (Parameter param : parameters) {
            if (param.getIn() == null) {
                continue;
            }
            ParameterDescriptor.ParameterLocation location = switch (param.getIn()) {
                case "path" -> ParameterDescriptor.ParameterLocation.PATH;
                case "query" -> ParameterDescriptor.ParameterLocation.QUERY;
                case "header" -> ParameterDescriptor.ParameterLocation.HEADER;
                default -> throw new IllegalArgumentException(
                        "Unsupported parameter location: " + param.getIn());
            };

            TypeDescriptor type = resolveSchemaType(param.getSchema());
            boolean required = param.getRequired() != null && param.getRequired();

            result.add(new ParameterDescriptor(param.getName(), location, type, required));
        }
        return result;
    }

    private TypeDescriptor resolveRequestBody(RequestBody requestBody) {
        if (requestBody == null || requestBody.getContent() == null) {
            return null;
        }

        Content content = requestBody.getContent();
        MediaType mediaType = content.get("application/json");
        if (mediaType == null && content.get("application/octet-stream") != null) {
            return TypeDescriptor.simple("org.springframework.core.io.Resource");
        }
        if (mediaType == null) {
            mediaType = content.values().iterator().next();
        }
        if (mediaType == null || mediaType.getSchema() == null) {
            return null;
        }

        return resolveSchemaType(mediaType.getSchema());
    }

    private TypeDescriptor resolveResponseType(ApiResponses responses) {
        if (responses == null) {
            return null;
        }

        ApiResponse successResponse = responses.get("200");
        if (successResponse == null) {
            return null;
        }

        Content content = successResponse.getContent();
        if (content == null) {
            return null;
        }

        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = content.values().iterator().next();
        }
        if (mediaType == null || mediaType.getSchema() == null) {
            return null;
        }

        return resolveSchemaType(mediaType.getSchema());
    }

    TypeDescriptor resolveSchemaType(Schema<?> schema) {
        if (schema == null) {
            return null;
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            if (schema.getAllOf().size() == 1) {
                return resolveSchemaType(schema.getAllOf().get(0));
            }
        }

        if (schema.get$ref() != null) {
            String refName = extractRefName(schema.get$ref());
            Schema<?> refSchema = componentSchemas.get(refName);
            if (refSchema != null) {
                if (refSchema.getEnum() != null && !refSchema.getEnum().isEmpty()) {
                    return TypeDescriptor.complex(refName);
                }
                if ("object".equals(refSchema.getType()) || refSchema.getType() == null) {
                    return TypeDescriptor.complex(refName);
                }
                return resolveSchemaType(refSchema);
            }
            return TypeDescriptor.complex(refName);
        }

        String type = schema.getType();

        if ("array".equals(type) && schema.getItems() != null) {
            TypeDescriptor elementType = resolveSchemaType(schema.getItems());
            return TypeDescriptor.list(elementType);
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            TypeDescriptor valueType = resolveSchemaType(additional);
            return TypeDescriptor.map(valueType);
        }

        if ("string".equals(type) && "binary".equals(schema.getFormat())) {
            return TypeDescriptor.simple("org.springframework.core.io.Resource");
        }

        return mapSimpleType(type, schema.getFormat());
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

    static String extractRefName(String ref) {
        int lastSlash = ref.lastIndexOf('/');
        return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
    }

    static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String camel = toCamelCase(input);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
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
