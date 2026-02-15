package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;

public record DecimalSchemaType(List<SchemaInstance> instances) implements JavaSchemaType {

    public DecimalSchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }
}
