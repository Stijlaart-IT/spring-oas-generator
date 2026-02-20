package nl.stijlaartit.spring.oas.generator.engine.schematype;


public sealed interface GeneratedSchemaType extends ConcreteSchemaType permits EnumSchemaType, ObjectSchemaType, UnionSchemaType {
}
