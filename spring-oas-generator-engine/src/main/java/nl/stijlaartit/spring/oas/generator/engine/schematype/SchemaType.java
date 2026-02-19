package nl.stijlaartit.spring.oas.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;

public sealed interface SchemaType permits JavaSchemaType, GeneratedSchemaType, RefSchemaType {

    List<SchemaInstance> instances();

    default Schema<?> schema() {
        return instances().getFirst().schema();
    }
}
