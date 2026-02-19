package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record MapSchemaType(List<SchemaInstance> instances, SchemaInstance valueInstance) implements JavaSchemaType {

    public MapSchemaType(List<SchemaInstance> instances, SchemaInstance valueInstance) {
        this.instances = List.copyOf(instances);
        this.valueInstance = Objects.requireNonNull(valueInstance);
    }
}
