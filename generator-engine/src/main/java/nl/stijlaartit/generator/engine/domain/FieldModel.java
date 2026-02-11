package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;

import java.util.Objects;

public class FieldModel {
    private String name;
    private String jsonName;
    private TypeDescriptor type;
    private boolean required;
    private boolean jsonValue;

    public FieldModel() {
    }

    public FieldModel(String name, String jsonName, TypeDescriptor type, boolean required) {
        this.name = Objects.requireNonNull(name);
        this.jsonName = Objects.requireNonNull(jsonName);
        this.type = Objects.requireNonNull(type);
        this.required = required;
        this.jsonValue = false;
    }

    public FieldModel(String name, String jsonName, TypeDescriptor type, boolean required, boolean jsonValue) {
        this.name = Objects.requireNonNull(name);
        this.jsonName = Objects.requireNonNull(jsonName);
        this.type = Objects.requireNonNull(type);
        this.required = required;
        this.jsonValue = jsonValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getJsonName() {
        return jsonName;
    }

    public void setJsonName(String jsonName) {
        this.jsonName = Objects.requireNonNull(jsonName);
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

    public boolean isJsonValue() {
        return jsonValue;
    }

    public void setJsonValue(boolean jsonValue) {
        this.jsonValue = jsonValue;
    }
}
