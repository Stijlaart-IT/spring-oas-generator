package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record UnionSchemaType(List<SchemaInstance> instances, String name,
                              List<SchemaInstance> variantInstances) implements GeneratedSchemaType {

    public UnionSchemaType(List<SchemaInstance> instances, String name, List<SchemaInstance> variantInstances) {
        this.instances = List.copyOf(instances);
        this.name = Objects.requireNonNull(name);
        this.variantInstances = List.copyOf(Objects.requireNonNull(variantInstances));
    }
}
