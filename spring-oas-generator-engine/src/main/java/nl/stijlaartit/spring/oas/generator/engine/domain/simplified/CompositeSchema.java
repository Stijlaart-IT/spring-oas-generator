package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Objects;

public record CompositeSchema(
        boolean isNullable,
        List<SimpleSchema> components
) implements SimpleSchema {

    public CompositeSchema {
        Objects.requireNonNull(components);
    }
}
