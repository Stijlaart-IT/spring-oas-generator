package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record SimpleObjectSchema(
        boolean isNullable,
        List<ObjectProperty> properties,
        Set<String> requiredProperties,
        Optional<SimpleSchema> additionalProperties
) implements SimpleSchema {

    public SimpleObjectSchema {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(requiredProperties);
        Objects.requireNonNull(additionalProperties);
    }
}
