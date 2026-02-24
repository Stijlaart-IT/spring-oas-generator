package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Objects;

public record IntegerEnumSchema(
        boolean isNullable,
        List<Integer> enumValues
) implements SimpleSchema {
    public IntegerEnumSchema {
        Objects.requireNonNull(enumValues);
    }
}
