package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;

public record BooleanSchemaType(List<SchemaInstance> instances) implements JavaSchemaType {

    public BooleanSchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }
}
