package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;

public record EmptySchemaType(List<SchemaInstance> instances) implements JavaSchemaType {
    public EmptySchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }

    @Override
    public JavaTypeName name() {
        return new JavaTypeName.Reserved("Object");
    }

}
