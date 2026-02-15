package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;

import java.util.Objects;

public record FieldModel(String name, String jsonName, TypeDescriptor type, boolean required, boolean nullable,
                         boolean jsonValue) {
    public FieldModel(String name, String jsonName, TypeDescriptor type,
                      boolean required, boolean nullable, boolean jsonValue) {
        this.name = Objects.requireNonNull(name);
        this.jsonName = Objects.requireNonNull(jsonName);
        this.type = Objects.requireNonNull(type);
        this.required = required;
        this.nullable = nullable;
        this.jsonValue = jsonValue;
    }
}
