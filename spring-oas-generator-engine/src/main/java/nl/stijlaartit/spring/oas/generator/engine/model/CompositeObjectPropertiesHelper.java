package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.CompositeSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ObjectProperty;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleObjectSchema;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.schematype.CompositeSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ConcreteSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.EnumSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.GeneratedSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.JavaSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.ObjectSchemaType;
import nl.stijlaartit.spring.oas.generator.engine.schematype.SchemaTypes;
import nl.stijlaartit.spring.oas.generator.engine.schematype.UnionSchemaType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CompositeObjectPropertiesHelper {

    sealed interface Result permits Result.Mixed, Result.Props {
        record Mixed(JavaTypeName javaTypeName, String error) implements Result {
        }

        record Props(Map<String, ObjectProperty> properties, Set<String> requiredProperties) implements Result {
            public Props {
                Objects.requireNonNull(properties);
                Objects.requireNonNull(requiredProperties);
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
                case ObjectSchemaType objectSchemaType -> {
                    if (objectSchemaType.schema() instanceof SimpleObjectSchema simpleObjectSchema) {
                        final Map<String, ObjectProperty> props = new LinkedHashMap<>();
                        for (ObjectProperty property : simpleObjectSchema.properties()) {
                            props.put(property.propertyName(), property);
                        }
                        yield new Result.Props(props, new LinkedHashSet<>(simpleObjectSchema.requiredProperties()));
                    }
                    yield new Result.Props(Map.of(), Set.of());
                }
                case EnumSchemaType enumSchemaType -> new Result.Mixed(enumSchemaType.name(), "Enum not supported in composite types");
                case UnionSchemaType unionSchemaType -> new Result.Mixed(unionSchemaType.name(), "Union not supported in composite types");
                case CompositeSchemaType compositeSchemaType -> collectNestedProperties(compositeSchemaType.schema());
            };
            case JavaSchemaType ignored -> new Result.Mixed(ignored.name(), "Primitives not supported");
        };
    }

    private Result collectNestedProperties(SimpleSchema schema) {
        if (!(schema instanceof CompositeSchema compositeSchema)) {
            return new Result.Props(Map.of(), Set.of());
        }
        List<SimpleSchema> allOf = Objects.requireNonNull(compositeSchema.components());
        // Use a linked hashmap to preserve order
        Map<String, ObjectProperty> properties = new LinkedHashMap<>();
        Set<String> requiredProperties = new LinkedHashSet<>();
        for (SimpleSchema part : allOf) {
            final var partSchemaType = schemaTypes.resolveConcrete(part);
            Result collectProperties = collectCompositeObjectProperties(partSchemaType);
            switch (collectProperties) {
                case Result.Mixed mixed -> {
                    return mixed;
                }
                case Result.Props props -> {
                    properties.putAll(props.properties());
                    requiredProperties.addAll(props.requiredProperties());
                }
            }
        }
        return new Result.Props(properties, requiredProperties);
    }

}
