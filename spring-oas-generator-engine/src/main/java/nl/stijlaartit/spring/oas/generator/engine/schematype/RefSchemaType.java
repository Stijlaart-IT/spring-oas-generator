package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record RefSchemaType(List<SchemaInstance> instances, String ref) implements SchemaType {

    public RefSchemaType(List<SchemaInstance> instances, String ref) {
        this.instances = List.copyOf(instances);
        this.ref = Objects.requireNonNull(ref);
    }
}
