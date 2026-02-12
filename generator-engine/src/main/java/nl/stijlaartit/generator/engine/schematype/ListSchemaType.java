package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public final class ListSchemaType implements JavaSchemaType {

    private final List<SchemaInstance> instances;
    private final SchemaInstance itemInstance;

    public ListSchemaType(List<SchemaInstance> instances, SchemaInstance itemInstance) {
        this.instances = List.copyOf(instances);
        this.itemInstance = Objects.requireNonNull(itemInstance);
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }

    public SchemaInstance itemInstance() {
        return itemInstance;
    }

    @Override
    public String javaTypeName() {
        return "java.util.List";
    }
}
