package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record EnumSchemaType(List<SchemaInstance> instances, JavaTypeName name) implements GeneratedSchemaType {

    public EnumSchemaType(List<SchemaInstance> instances, JavaTypeName name) {
        this.instances = List.copyOf(instances);
        this.name = Objects.requireNonNull(name);
    }
}
