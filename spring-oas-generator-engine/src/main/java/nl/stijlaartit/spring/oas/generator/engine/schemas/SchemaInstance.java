package nl.stijlaartit.spring.oas.generator.engine.schemas;

import nl.stijlaartit.spring.oas.generator.engine.domain.path.SchemaPath;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;

import java.util.Objects;

public record SchemaInstance(SimpleSchema schema, SchemaPath path) {
    public SchemaInstance {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(path);
    }
}
