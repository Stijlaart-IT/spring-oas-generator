package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record ObjectSchemaType(List<SchemaInstance> instances, JavaTypeName name) implements GeneratedSchemaType {

    public ObjectSchemaType(List<SchemaInstance> instances, JavaTypeName name) {
        this.instances = List.copyOf(instances);
        this.name = Objects.requireNonNull(name);
    }
}
