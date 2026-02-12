package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;

public final class DecimalSchemaType implements JavaSchemaType {

    private final List<SchemaInstance> instances;

    public DecimalSchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }

    @Override
    public String javaTypeName() {
        return "java.math.BigDecimal";
    }
}
