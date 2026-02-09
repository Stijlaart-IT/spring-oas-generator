package nl.stijlaartit.generation.model;

import nl.stijlaartit.generator.model.TypeDescriptor;

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
