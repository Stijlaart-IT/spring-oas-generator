package nl.stijlaartit.generator.engine.schematype;

import io.swagger.v3.oas.models.media.Schema;
import nl.stijlaartit.generator.engine.schemas.SchemaInstance;

import java.util.List;

public sealed interface SchemaType permits JavaSchemaType, GeneratedSchemaType, RefSchemaType {

    List<SchemaInstance> instances();

    default Schema<?> schema() {
        return instances().getFirst().schema();
    }
}
