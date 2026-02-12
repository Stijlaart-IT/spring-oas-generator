package nl.stijlaartit.generator.engine.model;

import java.util.Objects;

public record FieldDescriptor(
        String name,
        String jsonName,
        TypeDescriptor type,
        boolean required
) {
    public FieldDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(jsonName);
        Objects.requireNonNull(type);
    }
}
