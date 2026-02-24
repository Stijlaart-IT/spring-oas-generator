package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Objects;

public record StringEnumSchema(
        boolean isNullable,
        List<String> enumValues
) implements SimpleSchema {
    public StringEnumSchema {
        Objects.requireNonNull(enumValues);
    }
}
