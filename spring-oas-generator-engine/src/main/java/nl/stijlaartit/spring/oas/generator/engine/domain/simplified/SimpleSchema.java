package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

public sealed interface SimpleSchema
        permits SimpleStringSchema, SimpleNumberSchema, SimpleBooleanSchema, SimpleIntegerSchema, SimpleLongSchema, SimpleObjectSchema, SimpleArraySchema, SimpleAnySchema, CompositeSchema, UnionSchema, RefSchema, StringEnumSchema, NumberEnumSchema, IntegerEnumSchema, SimpleBinarySchema {

    boolean isNullable();
}
