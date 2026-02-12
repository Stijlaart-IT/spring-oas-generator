package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public final class ObjectSchemaType implements GeneratedSchemaType {

    private final List<SchemaInstance> instances;
    private final String name;

    public ObjectSchemaType(List<SchemaInstance> instances, String name) {
        this.instances = List.copyOf(instances);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }

    @Override
    public String name() {
        return name;
    }
}
