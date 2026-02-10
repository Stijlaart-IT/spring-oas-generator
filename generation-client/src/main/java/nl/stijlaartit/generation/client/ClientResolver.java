package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.model.TypeDescriptor;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClientResolver {

    private Map<String, Schema> componentSchemas = Map.of();
    private Map<String, Parameter> componentParameters = Map.of();

    public List<ClientDescriptor> resolve(OpenAPI openAPI) {
        componentSchemas = Map.of();
        componentParameters = Map.of();
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            componentSchemas = openAPI.getComponents().getSchemas();
        }
        if (openAPI.getComponents() != null && openAPI.getComponents().getParameters() != null) {
            componentParameters = openAPI.getComponents().getParameters();
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
        String operationId = operation.getOperationId();
        TypeDescriptor requestBody = resolveRequestBody(operationId, operation.getRequestBody());
        TypeDescriptor responseType = resolveResponseType(operationId, operation.getResponses());

        boolean deprecated = operation.getDeprecated() != null && operation.getDeprecated();
        OperationDescriptor descriptor = new OperationDescriptor(
                operation.getOperationId(), method, path, parameters, requestBody, responseType, deprecated
        );

        operationsByTag.computeIfAbsent(tag, k -> new ArrayList<>()).add(descriptor);
    }

    private List<ParameterDescriptor> resolveParameters(List<Parameter> parameters) {
        if (parameters == null) {
            return List.of();
        }

        List<ParameterDescriptor> result = new ArrayList<>();
        for (Parameter param : parameters) {
            Parameter resolved = resolveParameter(param);
            if (resolved == null || resolved.getIn() == null) {
                continue;
            }
            ParameterDescriptor.ParameterLocation location = switch (resolved.getIn()) {
                case "path" -> ParameterDescriptor.ParameterLocation.PATH;
                case "query" -> ParameterDescriptor.ParameterLocation.QUERY;
                case "header" -> ParameterDescriptor.ParameterLocation.HEADER;
                default -> throw new IllegalArgumentException(
                        "Unsupported parameter location: " + resolved.getIn());
            };

            TypeDescriptor type = resolved.getSchema() != null
                    ? resolveSchemaType(resolved.getSchema())
                    : TypeDescriptor.simple("java.lang.Object");
            boolean required = resolved.getRequired() != null && resolved.getRequired();

            result.add(new ParameterDescriptor(resolved.getName(), location, type, required));
        }
        return result;
    }

    private Parameter resolveParameter(Parameter parameter) {
        if (parameter == null || parameter.get$ref() == null) {
            return parameter;
        }
        Parameter current = parameter;
        java.util.Set<String> visitedRefs = new java.util.HashSet<>();
        while (current != null && current.get$ref() != null) {
            String refName = extractRefName(current.get$ref());
            if (!visitedRefs.add(refName)) {
                return null;
            }
            current = componentParameters.get(refName);
        }
        return current;
    }

    private TypeDescriptor resolveRequestBody(String operationId, RequestBody requestBody) {
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

        return resolveSchemaTypeForOperation(operationId, "Request", mediaType.getSchema());
    }

    private TypeDescriptor resolveResponseType(String operationId, ApiResponses responses) {
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

        return resolveSchemaTypeForOperation(operationId, "Response", mediaType.getSchema());
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

        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            return TypeDescriptor.simple("java.lang.Object");
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

        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return TypeDescriptor.simple("java.lang.String");
        }

        String type = resolveSchemaTypeName(schema);

        if ("array".equals(type) && schema.getItems() != null) {
            TypeDescriptor elementType = resolveSchemaType(schema.getItems());
            return TypeDescriptor.list(elementType);
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            TypeDescriptor valueType = resolveSchemaType(additional);
            return TypeDescriptor.map(valueType);
        }
        if (Boolean.TRUE.equals(schema.getAdditionalProperties())) {
            return TypeDescriptor.map(TypeDescriptor.simple("java.lang.Object"));
        }

        if ("string".equals(type) && "binary".equals(schema.getFormat())) {
            return TypeDescriptor.simple("org.springframework.core.io.Resource");
        }

        return mapSimpleType(type, schema.getFormat());
    }

    private TypeDescriptor resolveSchemaTypeForOperation(String operationId, String suffix, Schema<?> schema) {
        if (schema == null) {
            return null;
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            if (schema.getAllOf().size() == 1) {
                return resolveSchemaTypeForOperation(operationId, suffix, schema.getAllOf().get(0));
            }
        }

        if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
            String baseName = toPascalCase(operationId);
            if (baseName == null || baseName.isBlank()) {
                baseName = "Operation";
            }
            return TypeDescriptor.complex(baseName + suffix);
        }

        if (schema.get$ref() != null) {
            return resolveSchemaType(schema);
        }

        String baseName = toPascalCase(operationId);
        if (baseName == null || baseName.isBlank()) {
            baseName = "Operation";
        }
        String typeName = baseName + suffix;

        if (schema.getEnum() != null && !schema.getEnum().isEmpty()) {
            return TypeDescriptor.complex(typeName);
        }

        if (isObjectSchema(schema)) {
            return TypeDescriptor.complex(typeName);
        }

        String type = resolveSchemaTypeName(schema);
        if ("array".equals(type) && schema.getItems() != null) {
            TypeDescriptor elementType = resolveSchemaTypeForOperation(
                    operationId, suffix + "Item", schema.getItems());
            return TypeDescriptor.list(elementType);
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            TypeDescriptor valueType = resolveSchemaTypeForOperation(
                    operationId, suffix + "Value", additional);
            return TypeDescriptor.map(valueType);
        }

        return resolveSchemaType(schema);
    }

    private boolean isObjectSchema(Schema<?> schema) {
        if (!("object".equals(schema.getType()) || schema.getType() == null)) {
            return false;
        }
        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            return true;
        }
        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            for (Schema<?> part : schema.getAllOf()) {
                Schema<?> resolved = part.get$ref() != null
                        ? componentSchemas.getOrDefault(extractRefName(part.get$ref()), part)
                        : part;
                if (resolved.getProperties() != null && !resolved.getProperties().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
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
