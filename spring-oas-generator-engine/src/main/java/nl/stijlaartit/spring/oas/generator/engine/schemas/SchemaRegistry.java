package nl.stijlaartit.spring.oas.generator.engine.schemas;

import io.swagger.v3.oas.models.OpenAPI;
import nl.stijlaartit.spring.oas.generator.engine.Oas30ToSimpleSchemaMapper;
import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.PathRoot;
import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleArraySchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleParam;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOperation;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedRequest;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.UnionSchema;

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

    public static SchemaRegistry resolve(SimplifiedOas simplifiedOas) {
        Objects.requireNonNull(simplifiedOas);
        List<SchemaInstance> instances = new ArrayList<>();
        IdentityHashMap<SimpleSchema, Boolean> visiting = new IdentityHashMap<>();

        for (var entry : simplifiedOas.componentSchema().entrySet()) {
            SimpleSchema simpleSchema = entry.getValue();
            if (simpleSchema == null) {
                continue;
            }
            SchemaPath schemaPath = SchemaPath.forRoot(PathRoot.componentSchema(entry.getKey()));
            collect(simpleSchema, schemaPath, instances, visiting);
        }

        for (var entry : simplifiedOas.componentResponses().entrySet()) {
            SimpleSchema simpleSchema = entry.getValue();
            if (simpleSchema == null) {
                continue;
            }
            SchemaPath schemaPath = SchemaPath.forRoot(PathRoot.componentSchema(entry.getKey()));
            collect(simpleSchema, schemaPath, instances, visiting);
        }

        for (var entry : simplifiedOas.componentParameters().entrySet()) {
            SimpleSchema parameterSchema = entry.getValue();
            if (parameterSchema == null) {
                continue;
            }
            SchemaPath schemaPath = SchemaPath.forRoot(PathRoot.componentParameter(entry.getKey()));
            collect(parameterSchema, schemaPath, instances, visiting);
        }

        for (SimplifiedOperation operation : simplifiedOas.operations()) {
            collectOperation(operation, instances, visiting);
        }
        for (var pathParamsEntry : simplifiedOas.pathParams().entrySet()) {
            String path = pathParamsEntry.getKey();
            List<SimpleParam> params = pathParamsEntry.getValue();
            if (params == null || params.isEmpty()) {
                continue;
            }
            for (SimpleParam param : params) {
                if (param == null || param.schema() == null) {
                    continue;
                }
                collect(param.schema(), SchemaPath.forRoot(PathRoot.sharedPathParam(path, param.name())), instances, visiting);
            }
        }

        return new SchemaRegistry(instances);
    }

    private static void collectOperation(SimplifiedOperation operation,
                                         List<SchemaInstance> instances,
                                         IdentityHashMap<SimpleSchema, Boolean> visiting) {
        final String operationId = operation.operationId();
        final OperationName operationName = (operationId == null || operationId.isBlank()) ?
                OperationName.pathAndMethod(operation.path(), operation.method())
                : OperationName.id(operationId);

        if (operation.params() != null) {
            for (SimpleParam param : operation.params()) {
                if (param == null || param.schema() == null) {
                    continue;
                }
                collect(param.schema(), SchemaPath.forRoot(PathRoot.requestParam(operationName, param.name())), instances, visiting);
            }
        }

        if (operation.request() instanceof SimplifiedRequest.Json jsonRequest) {
            collect(jsonRequest.schema(), SchemaPath.forRoot(PathRoot.requestBody(operationName)), instances, visiting);
        }

        if (operation.responses() == null || operation.responses().isEmpty()) {
            return;
        }
        for (var response : operation.responses()) {
            if (response == null || response.schema() == null) {
                continue;
            }
            collect(response.schema(), SchemaPath.forRoot(PathRoot.responseBody(operationName, response.status())), instances, visiting);
        }
    }

    private static void collect(SimpleSchema schema,
                                SchemaPath schemaPath,
                                List<SchemaInstance> instances,
                                IdentityHashMap<SimpleSchema, Boolean> visiting) {
        if (visiting.put(schema, Boolean.TRUE) != null) {
            throw new IllegalStateException("Detected schema cycle during registry collection.");
        }

        instances.add(new SchemaInstance(schema, schemaPath));

        switch (schema) {
            case CompositeSchema compositeSchema -> {
                if (compositeSchema.components() != null) {
                    if (compositeSchema.components().size() == 1) {
                        collect(compositeSchema.components().getFirst(), schemaPath.singletonVariant("allOf"), instances, visiting);
                    } else {
                        for (int i = 0; i < compositeSchema.components().size(); i++) {
                            collect(compositeSchema.components().get(i), schemaPath.variant("allOf", i), instances, visiting);
                        }
                    }
                }
            }
            case UnionSchema unionSchema -> {
                if (unionSchema.variants() != null) {
                    for (int i = 0; i < unionSchema.variants().size(); i++) {
                        collect(unionSchema.variants().get(i), schemaPath.variant("oneOf", i), instances, visiting);
                    }
                }
            }
            case SimpleArraySchema arraySchema -> collect(arraySchema.itemSchema(), schemaPath.items(), instances, visiting);
            case SimpleObjectSchema objectSchema -> {
                for (int i = 0; i < objectSchema.properties().size(); i++) {
                    ObjectProperty property = objectSchema.properties().get(i);
                    if (property == null || property.schema() == null) {
                        continue;
                    }
                    collect(property.schema(), schemaPath.property(property.propertyName()), instances, visiting);
                }
                objectSchema.additionalProperties().ifPresent(simpleSchema ->
                        collect(simpleSchema, schemaPath.additionalProperties(), instances, visiting)
                );
            }
            default -> {
            }
        }

        visiting.remove(schema);
    }

    public SchemaInstance instanceForSchema(SimpleSchema schema) {
        return instances.stream()
                .filter(i -> i.schema().equals(schema))
                .findFirst()
                .orElseThrow();
    }
}
