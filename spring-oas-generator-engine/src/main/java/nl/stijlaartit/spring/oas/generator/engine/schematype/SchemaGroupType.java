package nl.stijlaartit.spring.oas.generator.engine.schematype;

// TODO Can this be inlined?
public enum SchemaGroupType {
    BOOLEAN,
    INTEGER,
    NUMBER,
    STRING,
    ENUM,
    ARRAY,
    OBJECT,
    REF,
    ALL_OF_EMPTY,
    ALL_OF_SINGLE,
    ALL_OF_MULTI,
    ONE_OF_EMPTY,
    ONE_OF_SINGLE,
    ONE_OF_MULTI,
    ANY_OF_EMPTY,
    ANY_OF_SINGLE,
    ANY_OF_MULTI,
    EMPTY,
    INVALID
}
