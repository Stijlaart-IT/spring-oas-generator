package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;

public final class IntegerSchemaType implements JavaSchemaType {

    private final List<SchemaInstance> instances;

    public IntegerSchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }

    @Override
    public String javaTypeName() {
        return "java.lang.Integer";
    }
}
