package nl.stijlaartit.generator.engine.schemas;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
                SchemaParent parent = new SchemaParent.ComponentParent(entry.getKey());
                String path = appendPath("$.components.schemas", entry.getKey());
                collect(schema, parent, path, instances, visiting);
            }
        }

        if (openAPI.getComponents() != null && openAPI.getComponents().getParameters() != null) {
            for (var entry : openAPI.getComponents().getParameters().entrySet()) {
                Parameter parameter = entry.getValue();
                if (parameter == null) {
                    continue;
                }
                SchemaParent parent = new SchemaParent.ComponentParameterParent(entry.getKey());
                String base = appendPath("$.components.parameters", entry.getKey());
                collectParameterSchema(parameter, parent, base, instances, visiting);
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
                collectOperation(path, PathItem.HttpMethod.GET, pathItem.getGet(), instances, visiting);
                collectOperation(path, PathItem.HttpMethod.POST, pathItem.getPost(), instances, visiting);
                collectOperation(path, PathItem.HttpMethod.PUT, pathItem.getPut(), instances, visiting);
                collectOperation(path, PathItem.HttpMethod.DELETE, pathItem.getDelete(), instances, visiting);
                collectOperation(path, PathItem.HttpMethod.PATCH, pathItem.getPatch(), instances, visiting);
            }
        }

        return new SchemaRegistry(instances);
    }

    private static void collectOperation(String path,
                                         PathItem.HttpMethod method,
                                         Operation operation,
                                         List<SchemaInstance> instances,
                                         IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (operation == null) {
            return;
        }

        if (operation.getParameters() != null && !operation.getParameters().isEmpty()) {
            String base = appendPath("$.paths", path);
            base = appendPath(base, method != null ? method.name().toLowerCase() : "operation");
            for (int i = 0; i < operation.getParameters().size(); i++) {
                Parameter parameter = operation.getParameters().get(i);
                if (parameter == null) {
                    continue;
                }
                SchemaParent parent = new SchemaParent.OperationParameterParent(
                        operation, method, path, parameter.getName(), parameter.getIn());
                String parameterPath = appendIndex(appendPath(base, "parameters"), i);
                collectParameterSchema(parameter, parent, parameterPath, instances, visiting);
            }
        }

        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Schema<?> requestSchema = resolveContentSchema(operation.getRequestBody().getContent());
            if (requestSchema != null) {
                SchemaParent parent = new SchemaParent.OperationRequestParent(operation, method, path);
                String base = appendPath("$.paths", path);
                base = appendPath(base, method != null ? method.name().toLowerCase() : "operation");
                base = appendPath(base, "requestBody");
                base = appendPath(base, "content");
                base = appendPath(base, "application/json");
                String jsonPath = appendPath(base, "schema");
                collect(requestSchema, parent, jsonPath, instances, visiting);
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
                SchemaParent parent = new SchemaParent.OperationResponseParent(operation, status, method, path);
                String base = appendPath("$.paths", path);
                base = appendPath(base, method != null ? method.name().toLowerCase() : "operation");
                base = appendPath(base, "responses");
                base = appendPath(base, status);
                base = appendPath(base, "content");
                base = appendPath(base, "application/json");
                String jsonPath = appendPath(base, "schema");
                collect(responseSchema, parent, jsonPath, instances, visiting);
            }
        }
    }

    private static void collectPathItemParameters(String path,
                                                  PathItem pathItem,
                                                  List<SchemaInstance> instances,
                                                  IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (pathItem == null || pathItem.getParameters() == null || pathItem.getParameters().isEmpty()) {
            return;
        }
        String base = appendPath("$.paths", path);
        for (int i = 0; i < pathItem.getParameters().size(); i++) {
            Parameter parameter = pathItem.getParameters().get(i);
            if (parameter == null) {
                continue;
            }
            SchemaParent parent = new SchemaParent.OperationParameterParent(
                    null, null, path, parameter.getName(), parameter.getIn());
            String parameterPath = appendIndex(appendPath(base, "parameters"), i);
            collectParameterSchema(parameter, parent, parameterPath, instances, visiting);
        }
    }

    private static Schema<?> resolveContentSchema(Content content) {
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
                                               SchemaParent parent,
                                               String basePath,
                                               List<SchemaInstance> instances,
                                               IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (parameter == null) {
            return;
        }
        if (parameter.getSchema() != null) {
            String jsonPath = appendPath(basePath, "schema");
            collect(parameter.getSchema(), parent, jsonPath, instances, visiting);
        }
        if (parameter.getContent() != null) {
            Schema<?> contentSchema = resolveContentSchema(parameter.getContent());
            if (contentSchema != null) {
                String contentBase = appendPath(basePath, "content");
                contentBase = appendPath(contentBase, "application/json");
                String jsonPath = appendPath(contentBase, "schema");
                collect(contentSchema, parent, jsonPath, instances, visiting);
            }
        }
    }

    private static void collect(Schema<?> schema,
                                SchemaParent parent,
                                String jsonPath,
                                List<SchemaInstance> instances,
                                IdentityHashMap<Schema<?>, Boolean> visiting) {
        if (schema == null) {
            return;
        }

        if (visiting.put(schema, Boolean.TRUE) != null) {
            throw new IllegalStateException("Detected schema cycle during registry collection.");
        }

        SchemaInstance instance = new SchemaInstance(schema, parent, jsonPath);
        instances.add(instance);
        if (schema.getAllOf() != null) {
            for (int i = 0; i < schema.getAllOf().size(); i++) {
                Schema<?> part = schema.getAllOf().get(i);
                SchemaParent.SchemaRelation relation = new SchemaParent.SchemaRelation.AllOfRelation(i);
                SchemaParent nestedParent = new SchemaParent.SchemaInstanceParent(instance, relation);
                String partPath = appendIndex(appendPath(jsonPath, "allOf"), i);
                collect(part, nestedParent, partPath, instances, visiting);
            }
        }
        if (schema.getOneOf() != null) {
            List<Schema> parts = schema.getOneOf();
            for (int i = 0; i < parts.size(); i++) {
                Schema<?> part = parts.get(i);
                SchemaParent.SchemaRelation relation = new SchemaParent.SchemaRelation.OneOfRelation(i);
                SchemaParent nestedParent = new SchemaParent.SchemaInstanceParent(instance, relation);
                String partPath = appendIndex(appendPath(jsonPath, "oneOf"), i);
                collect(part, nestedParent, partPath, instances, visiting);
            }
        }
        if (schema.getAnyOf() != null) {
            for (int i = 0; i < schema.getAnyOf().size(); i++) {
                Schema<?> part = schema.getAnyOf().get(i);
                SchemaParent.SchemaRelation relation = new SchemaParent.SchemaRelation.AnyOfRelation(i);
                SchemaParent nestedParent = new SchemaParent.SchemaInstanceParent(instance, relation);
                String partPath = appendIndex(appendPath(jsonPath, "anyOf"), i);
                collect(part, nestedParent, partPath, instances, visiting);
            }
        }
        if (schema.getProperties() != null) {
            for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                SchemaParent.SchemaRelation relation =
                        new SchemaParent.SchemaRelation.PropertyRelation(entry.getKey());
                SchemaParent nestedParent = new SchemaParent.SchemaInstanceParent(instance, relation);
                String propertyPath = appendPath(appendPath(jsonPath, "properties"), entry.getKey());
                collect(entry.getValue(), nestedParent, propertyPath, instances, visiting);
            }
        }
        if (schema.getItems() != null) {
            SchemaParent.SchemaRelation relation = new SchemaParent.SchemaRelation.ListItemRelation();
            SchemaParent nestedParent = new SchemaParent.SchemaInstanceParent(instance, relation);
            String itemPath = appendPath(jsonPath, "items");
            collect(schema.getItems(), nestedParent, itemPath, instances, visiting);
        }
        if (schema.getAdditionalProperties() instanceof Schema<?> additional) {
            SchemaParent.SchemaRelation relation = new SchemaParent.SchemaRelation.AdditionalPropertiesRelation();
            SchemaParent nestedParent = new SchemaParent.SchemaInstanceParent(instance, relation);
            String additionalPath = appendPath(jsonPath, "additionalProperties");
            collect(additional, nestedParent, additionalPath, instances, visiting);
        }

        visiting.remove(schema);
    }

    private static String appendPath(String base, String segment) {
        if (base == null || base.isBlank()) {
            base = "$";
        }
        if (segment == null || segment.isBlank()) {
            return base;
        }
        if (segment.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return base + "." + segment;
        }
        String escaped = segment.replace("\\", "\\\\").replace("'", "\\'");
        return base + "['" + escaped + "']";
    }

    private static String appendIndex(String base, int index) {
        return base + "[" + index + "]";
    }

    @NonNull
    public SchemaInstance instanceForSchema(Schema<?> schema) {
        return instances.stream()
                .filter(i -> i.schema().equals(schema))
                .findFirst()
                .orElseThrow();
    }
}
