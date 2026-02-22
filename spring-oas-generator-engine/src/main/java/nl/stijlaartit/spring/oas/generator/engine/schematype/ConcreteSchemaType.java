package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;

public sealed interface ConcreteSchemaType extends SchemaType permits JavaSchemaType, GeneratedSchemaType {
    JavaTypeName name();
}
