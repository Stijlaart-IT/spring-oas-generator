package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.model.TypeDescriptor;

import java.util.Objects;

public record ParameterDescriptor(
        String name,
        ParameterLocation location,
        TypeDescriptor type,
        boolean required
) {
    public ParameterDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(location);
        Objects.requireNonNull(type);
    }

    public enum ParameterLocation {
        PATH, QUERY, HEADER
    }
}
