package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Objects;
import java.math.BigDecimal;

public record NumberEnumSchema(
        boolean isNullable,
        List<BigDecimal> enumValues
) implements SimpleSchema {
    public NumberEnumSchema {
        Objects.requireNonNull(enumValues);
    }
}
