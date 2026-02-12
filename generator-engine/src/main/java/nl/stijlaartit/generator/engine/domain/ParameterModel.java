package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;

import java.util.Objects;

public class ParameterModel {
    private final String name;
    private final ParameterLocation location;
    private final TypeDescriptor type;
    private final boolean required;

    public ParameterModel(String name, ParameterLocation location, TypeDescriptor type, boolean required) {
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
        this.type = Objects.requireNonNull(type);
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public ParameterLocation getLocation() {
        return location;
    }

    public TypeDescriptor getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }
}
