package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;

import java.util.Objects;

public record ParameterModel(String name, ParameterLocation location, TypeDescriptor type, boolean required) {
    public ParameterModel(String name, ParameterLocation location, TypeDescriptor type, boolean required) {
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
        this.type = Objects.requireNonNull(type);
        this.required = required;
    }
}
