package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.Objects;

public record ObjectProperty(String propertyName, SimpleSchema schema) {

    public ObjectProperty {
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(schema);
    }
}
