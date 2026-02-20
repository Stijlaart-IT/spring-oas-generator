package nl.stijlaartit.spring.oas.generator.engine.schematype;

import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;

public sealed interface ConcreteSchemaType extends SchemaType permits JavaSchemaType, GeneratedSchemaType {
    JavaTypeName name();
}
