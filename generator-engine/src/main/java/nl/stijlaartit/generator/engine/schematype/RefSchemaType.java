package nl.stijlaartit.generator.engine.schematype;

import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public final class RefSchemaType implements SchemaType {

    private final List<SchemaInstance> instances;
    private final String ref;

    public RefSchemaType(List<SchemaInstance> instances, String ref) {
        this.instances = List.copyOf(instances);
        this.ref = Objects.requireNonNull(ref);
    }

    @Override
    public List<SchemaInstance> instances() {
        return instances;
    }

    public String ref() {
        return ref;
    }
}
