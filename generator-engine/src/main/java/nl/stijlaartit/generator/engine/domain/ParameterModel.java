package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;

import java.util.Objects;

public class ParameterModel {
    private String name;
    private ParameterLocation location;
    private TypeDescriptor type;
    private boolean required;

    public ParameterModel() {
    }

    public ParameterModel(String name, ParameterLocation location, TypeDescriptor type, boolean required) {
        this.name = Objects.requireNonNull(name);
        this.location = Objects.requireNonNull(location);
        this.type = Objects.requireNonNull(type);
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public ParameterLocation getLocation() {
        return location;
    }

    public void setLocation(ParameterLocation location) {
        this.location = Objects.requireNonNull(location);
    }

    public TypeDescriptor getType() {
        return type;
    }

    public void setType(TypeDescriptor type) {
        this.type = Objects.requireNonNull(type);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
