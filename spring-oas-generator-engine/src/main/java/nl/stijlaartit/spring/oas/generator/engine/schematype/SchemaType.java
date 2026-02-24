package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;

public sealed interface SchemaType permits ConcreteSchemaType, RefSchemaType, DeferredSchemaType {

    List<SchemaInstance> instances();

    default SimpleSchema schema() {
        return instances().getFirst().schema();
    }
}
