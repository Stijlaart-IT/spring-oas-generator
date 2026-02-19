package nl.stijlaartit.spring.oas.generator.engine.domain;

import java.util.List;
import java.util.Objects;

public record RecordModel(String name, List<FieldModel> fields, boolean additionalProperties) implements ModelFile {
    public RecordModel(String name, List<FieldModel> fields, boolean additionalProperties) {
        this.name = Objects.requireNonNull(name);
        this.fields = List.copyOf(fields);
        this.additionalProperties = additionalProperties;
    }
}
