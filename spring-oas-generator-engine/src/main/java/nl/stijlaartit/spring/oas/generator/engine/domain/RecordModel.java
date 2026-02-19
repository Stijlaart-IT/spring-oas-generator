package nl.stijlaartit.spring.oas.generator.engine.domain;

import java.util.List;
import java.util.Objects;

public record RecordModel(String name, List<FieldModel> fields) implements ModelFile {
    public RecordModel(String name, List<FieldModel> fields) {
        this.name = Objects.requireNonNull(name);
        this.fields = List.copyOf(fields);
    }
}
