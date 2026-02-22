package nl.stijlaartit.spring.oas.generator.domain.file;

import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaParameterName;

import java.util.Objects;

public record RecordField(JavaParameterName name, String jsonName, TypeDescriptor type, boolean required, boolean nullable,
                          boolean jsonValue) {
    public RecordField(JavaParameterName name, String jsonName, TypeDescriptor type,
                       boolean required, boolean nullable, boolean jsonValue) {
        this.name = Objects.requireNonNull(name);
        this.jsonName = Objects.requireNonNull(jsonName);
        this.type = Objects.requireNonNull(type);
        this.required = required;
        this.nullable = nullable;
        this.jsonValue = jsonValue;
    }
}
