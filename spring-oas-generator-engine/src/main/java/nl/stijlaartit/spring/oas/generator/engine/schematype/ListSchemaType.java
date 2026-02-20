package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;
import java.util.Objects;

public record ListSchemaType(List<SchemaInstance> instances, SchemaInstance itemInstance) implements JavaSchemaType {

    public ListSchemaType(List<SchemaInstance> instances, SchemaInstance itemInstance) {
        this.instances = List.copyOf(instances);
        this.itemInstance = Objects.requireNonNull(itemInstance);
    }

    @Override
    public JavaTypeName name() {
        return new JavaTypeName.Reserved("List");
    }

}
