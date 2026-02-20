package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record RefSchemaType(List<SchemaInstance> instances, SchemaRef ref) implements SchemaType {

    public RefSchemaType(List<SchemaInstance> instances, SchemaRef ref) {
        this.instances = List.copyOf(instances);
        this.ref = Objects.requireNonNull(ref);
    }
}
