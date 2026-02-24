package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.Objects;

public record SimpleArraySchema(
        boolean isNullable,
        SimpleSchema itemSchema
) implements SimpleSchema {

    public SimpleArraySchema {
        Objects.requireNonNull(itemSchema);
    }
}
