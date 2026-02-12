package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public final class MapSchemaType implements JavaSchemaType {

    private final List<SchemaInstance> instances;
    private final SchemaInstance valueInstance;

    public MapSchemaType(List<SchemaInstance> instances, SchemaInstance valueInstance) {
        this.instances = List.copyOf(instances);
        this.valueInstance = Objects.requireNonNull(valueInstance);
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }

    public SchemaInstance valueInstance() {
        return valueInstance;
    }

    @Override
    public String javaTypeName() {
        return "java.util.Map";
    }
}
