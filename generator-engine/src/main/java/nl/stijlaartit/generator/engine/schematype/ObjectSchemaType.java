package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record ObjectSchemaType(List<SchemaInstance> instances, String name) implements GeneratedSchemaType {

    public ObjectSchemaType(List<SchemaInstance> instances, String name) {
        this.instances = List.copyOf(instances);
        this.name = Objects.requireNonNull(name);
    }
}
