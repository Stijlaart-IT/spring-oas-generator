package nl.stijlaartit.generator.engine.schematype;

public sealed interface GeneratedSchemaType extends SchemaType permits EnumSchemaType, ObjectSchemaType, UnionSchemaType {
    String name();
}
