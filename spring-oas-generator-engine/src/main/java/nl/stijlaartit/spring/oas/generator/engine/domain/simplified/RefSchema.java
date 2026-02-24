package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import nl.stijlaartit.spring.oas.generator.engine.domain.SchemaRef;

import java.util.Objects;

public record RefSchema(boolean isNullable, SchemaRef ref) implements SimpleSchema {

    public RefSchema {
        Objects.requireNonNull(ref);
    }
}
