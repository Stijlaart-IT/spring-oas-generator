package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;

public class EmptySchemaType implements JavaSchemaType {
    private final List<SchemaInstance> instances;

    public EmptySchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }


    @Override
    public String javaTypeName() {
        return "java.lang.Object";
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }
}
