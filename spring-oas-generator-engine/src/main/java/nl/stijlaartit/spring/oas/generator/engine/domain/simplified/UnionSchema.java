package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Objects;

public record UnionSchema(
        boolean isNullable,
        List<SimpleSchema> variants,
        String discriminatorProperty
) implements SimpleSchema {

    public UnionSchema {
        Objects.requireNonNull(variants);
    }
}
