package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;

import java.util.List;

public record CompositeSchemaType(JavaTypeName name,List<SchemaInstance> instances) implements GeneratedSchemaType {
}
