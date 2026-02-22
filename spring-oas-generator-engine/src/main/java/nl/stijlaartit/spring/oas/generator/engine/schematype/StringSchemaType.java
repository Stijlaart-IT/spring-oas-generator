package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;

public record StringSchemaType(List<SchemaInstance> instances) implements JavaSchemaType {

    public StringSchemaType(List<SchemaInstance> instances) {
        this.instances = List.copyOf(instances);
    }


    @Override
    public JavaTypeName name() {
        return new JavaTypeName.Reserved("String");
    }

}
