package nl.stijlaartit.spring.oas.generator.engine.schemas;

import io.swagger.v3.oas.models.media.Schema;

import java.util.Objects;

public record SchemaInstance(Schema<?> schema, SchemaParent parent) {
    public SchemaInstance(Schema<?> schema, SchemaParent parent) {
        this.schema = Objects.requireNonNull(schema);
        this.parent = Objects.requireNonNull(parent);
    }
}
