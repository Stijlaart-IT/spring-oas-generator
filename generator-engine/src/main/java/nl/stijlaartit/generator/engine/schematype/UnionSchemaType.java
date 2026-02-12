package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public final class UnionSchemaType implements GeneratedSchemaType {

    private final List<SchemaInstance> instances;
    private final String name;
    private final List<SchemaInstance> variantInstances;

    public UnionSchemaType(List<SchemaInstance> instances, String name, List<SchemaInstance> variantInstances) {
        this.instances = List.copyOf(instances);
        this.name = Objects.requireNonNull(name);
        this.variantInstances = List.copyOf(Objects.requireNonNull(variantInstances));
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }

    public List<SchemaInstance> variantInstances() {
        return variantInstances;
    }

    @Override
    public String name() {
        return name;
    }
}
