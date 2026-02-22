package nl.stijlaartit.spring.oas.generator.engine.schemas;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;

public final class SchemaRegistry {
    private final List<SchemaInstance> instances;

    private SchemaRegistry(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }

    public List<SchemaInstance> getInstances() {
        return instances;
    }

    /**
     * Collects schemas from components, parameters, request bodies, and response bodies.
     *
     * <p>Note: schemas should not be cyclic at this stage (directly or indirectly), since
     * cycles should only be introduced through $ref. References are not resolved during
     * collection. A cycle detected by object reference indicates an invalid in-memory model
     * and will raise an error.
     */
    public static SchemaRegistry resolve(OpenAPI openAPI) {
        Objects.requireNonNull(openAPI);
        List<SchemaInstance> instances = new ArrayList<>();
        IdentityHashMap<Schema<?>, Boolean> visiting = new IdentityHashMap<>();

        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            for (var entry : openAPI.getComponents().getSchemas().entrySet()) {
                Schema<?> schema = entry.getValue();
                if (schema == null) {
                    continue;
                }

                SchemaPath schemaPath = SchemaPath.forRoot(PathRoot.componentSchema(entry.getKey()));
                collect(schema, schemaPath, instances, visiting);
            }
        }

        if (openAPI.getComponents() != null && openAPI.getComponents().getParameters() != null) {
            for (var entry : openAPI.getComponents().getParameters().entrySet()) {
                Parameter parameter = entry.getValue();
                if (parameter == null) {
                    continue;
                }
                collectParameterSchema(parameter, SchemaPath.forRoot(PathRoot.componentParameter(entry.getKey())), instances, visiting);
            }
        }

        if (openAPI.getPaths() != null) {
            for (var pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                if (pathItem == null) {
                    continue;
                }
                collectPathItemParameters(path, pathItem, instances, visiting);
                collectOperation(path, HttpMethod.GET, pathItem.getGet(), instances, visiting);
                collectOperation(path, HttpMethod.POST, pathItem.getPost(), instances, visiting);
                collectOperation(path, HttpMethod.PUT, pathItem.getPut(), instances, visiting);
                collectOperation(path, HttpMethod.DELETE, pathItem.getDelete(), instances, visiting);
                collectOperation(path, HttpMethod.PATCH, pathItem.getPatch(), instances, visiting);
            }
        }

        return new SchemaRegistry(instances);
    }

    private static void collectOperation(String path,
                                         HttpMethod method,
                                         @Nullable
                                         Operation operation,
                                         List<SchemaInstance> instances,
                                         IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (operation == null) {
            return;
        }

        String operationId = operation.getOperationId();
        final OperationName operationName = (operationId == null || operationId.isBlank()) ?
                OperationName.pathAndMethod(path, method)
                : OperationName.id(operationId);

        if (operation.getParameters() != null && !operation.getParameters().isEmpty()) {
            for (int i = 0; i < operation.getParameters().size(); i++) {
                Parameter parameter = operation.getParameters().get(i);
                if (parameter == null) {
                    continue;
                }
                String name = parameter.getName() != null ? parameter.getName() : "Index" + (i + 1);
                collectParameterSchema(parameter, SchemaPath.forRoot(PathRoot.requestParam(operationName, name)), instances, visiting);
            }
        }

        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Schema<?> requestSchema = resolveContentSchema(operation.getRequestBody().getContent());
            if (requestSchema != null) {
                collect(requestSchema, SchemaPath.forRoot(PathRoot.requestBody(operationName)), instances, visiting);
            }
        }

        if (operation.getResponses() != null) {
            for (var entry : operation.getResponses().entrySet()) {
                String status = entry.getKey();
                ApiResponse response = entry.getValue();
                if (response == null || response.getContent() == null) {
                    continue;
                }
                Schema<?> responseSchema = resolveContentSchema(response.getContent());
                if (responseSchema == null) {
                    continue;
                }
                collect(responseSchema, SchemaPath.forRoot(PathRoot.responseBody(operationName, status)), instances, visiting);
            }
        }
    }

    private static void collectPathItemParameters(String path,
                                                  PathItem pathItem,
                                                  List<SchemaInstance> instances,
                                                  IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (pathItem.getParameters() == null || pathItem.getParameters().isEmpty()) {
            return;
        }
        for (int i = 0; i < pathItem.getParameters().size(); i++) {
            Parameter parameter = pathItem.getParameters().get(i);
            if (parameter == null) {
                continue;
            }
            collectParameterSchema(parameter, SchemaPath.forRoot(PathRoot.sharedPathParam(path, parameter.getName())), instances, visiting);
        }
    }

    @Nullable
    private static Schema<?> resolveContentSchema(@Nullable Content content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = content.values().iterator().next();
        }
        return mediaType != null ? mediaType.getSchema() : null;
    }

    private static void collectParameterSchema(Parameter parameter,
                                               SchemaPath schemaPath,
                                               List<SchemaInstance> instances,
                                               IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (parameter.getSchema() != null) {
            collect(parameter.getSchema(), schemaPath, instances, visiting);
        }
        // TODO Document compatibility
//        if (parameter.getContent() != null) {
//            Schema<?> contentSchema = resolveContentSchema(parameter.getContent());
//            if (contentSchema != null) {
//                String contentBase = appendPath(basePath, "content");
//                contentBase = appendPath(contentBase, "application/json");
//                String jsonPath = appendPath(contentBase, "schema");
//                collect(contentSchema, parent, new SchemaPath(SchemaPath.PathRoot.REQUEST_PARAM_SCHEMA, List.of()), jsonPath, instances, visiting);
//            }
//        }
    }

    private static void collect(Schema<?> schema,
                                SchemaPath schemaPath,
                                List<SchemaInstance> instances,
                                IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (visiting.put(schema, Boolean.TRUE) != null) {
            throw new IllegalStateException("Detected schema cycle during registry collection.");
        }

        SchemaInstance instance = new SchemaInstance(schema, schemaPath);
        instances.add(instance);
        if (schema.getAllOf() != null) {
            if (schema.getAllOf().size() == 1) {
                Schema<?> part = schema.getAllOf().getFirst();
                final var nestedPath = schemaPath.singletonVariant("allOf");
                collect(part, nestedPath, instances, visiting);
            } else {
                for (int i = 0; i < schema.getAllOf().size(); i++) {
                    Schema<?> part = schema.getAllOf().get(i);
                    final var nestedPath = schemaPath.variant("allOf", i);
                    collect(part, nestedPath, instances, visiting);
                }
            }
        }
        if (schema.getOneOf() != null) {
            final var parts = schema.getOneOf();
            for (int i = 0; i < parts.size(); i++) {
                Schema<?> part = parts.get(i);
                final var nestedPath = schemaPath.variant("oneOf", i);
                collect(part, nestedPath, instances, visiting);
            }
        }
        if (schema.getAnyOf() != null) {
            for (int i = 0; i < schema.getAnyOf().size(); i++) {
                Schema<?> part = schema.getAnyOf().get(i);
                final var nestedPath = schemaPath.variant("anyOf", i);
                collect(part, nestedPath, instances, visiting);
            }
        }
        if (schema.getProperties() != null) {
            for (final var entry : schema.getProperties().entrySet()) {
                final var nestedPath = schemaPath.property(entry.getKey());

                collect(entry.getValue(), nestedPath, instances, visiting);
            }
        }
        if (schema.getItems() != null) {
            collect(schema.getItems(), schemaPath.items(), instances, visiting);
        }
        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            collect(additional, schemaPath.additionalProperties(), instances, visiting);
        }

        visiting.remove(schema);
    }

    public SchemaInstance instanceForSchema(Schema<?> schema) {
        return instances.stream()
                .filter(i -> i.schema().equals(schema))
                .findFirst()
                .orElseThrow();
    }
}
