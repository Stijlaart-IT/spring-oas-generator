package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;

public record IntegerSchemaType(List<SchemaInstance> instances) implements JavaSchemaType {

    public IntegerSchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }
}
