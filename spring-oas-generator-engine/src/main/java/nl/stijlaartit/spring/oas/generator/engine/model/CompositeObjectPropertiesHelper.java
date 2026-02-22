package nl.stijlaartit.spring.oas.generator.engine.model;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.schematype.CompositeSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ConcreteSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.EnumSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ObjectSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.spring.oas.generator.engine.schematype.UnionSchemaType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CompositeObjectPropertiesHelper {

    sealed interface Result permits Result.Mixed, Result.Props {
        record Mixed(String error)implements Result {}
        record Props(Map<String, Schema> properties) implements Result {
            public Props {
                // TODO unit test
                Objects.requireNonNull(properties);
            }
        }

    }
    private final SchemaTypes schemaTypes;

    public CompositeObjectPropertiesHelper(SchemaTypes schemaTypes) {
        this.schemaTypes = schemaTypes;
    }

    public Result collectCompositeObjectProperties(ConcreteSchemaType concreteSchemaType) {
        return switch (concreteSchemaType) {
            case GeneratedSchemaType generatedSchemaType -> switch (generatedSchemaType) {
                case ObjectSchemaType objectSchemaType -> new Result.Props(concreteSchemaType.schema().getProperties() != null ? concreteSchemaType.schema().getProperties() : new HashMap<>());
                case EnumSchemaType enumSchemaType -> new Result.Mixed("Enum not supported in composite types");
                case UnionSchemaType unionSchemaType -> new Result.Mixed("Union not supported in composite types");
                case CompositeSchemaType compositeSchemaType -> collectNestedProperties(compositeSchemaType.schema());
            };
            // TODO, what about Map?
            case JavaSchemaType ignored -> new Result.Mixed("Primitives not supporte");
        };
    }

    private Result collectNestedProperties(Schema<?> schema) {
        List<Schema> allOf = Objects.requireNonNull(schema.getAllOf());
        // Use linked hashmap to preserve order
        Map<String, Schema> properties = new LinkedHashMap<>();
        for (Schema<?> part : allOf) {
            final var partSchemaType = schemaTypes.resolveConcrete(part);
            Result collectProperties = collectCompositeObjectProperties(partSchemaType);
            switch (collectProperties) {
                case Result.Mixed mixed -> {
                    return mixed;
                }
                case Result.Props props -> {
                    properties.putAll(props.properties());
                }
            }
        }
        return new Result.Props(properties);
    }

}
