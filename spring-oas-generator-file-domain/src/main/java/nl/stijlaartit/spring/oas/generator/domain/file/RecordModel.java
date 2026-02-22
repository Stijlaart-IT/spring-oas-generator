package nl.stijlaartit.spring.oas.generator.domain.file;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;

import java.util.List;
import java.util.Objects;

public record RecordModel(JavaTypeName typeName, List<RecordField> fields, boolean additionalProperties) implements ModelFile {
    public RecordModel(JavaTypeName typeName, List<RecordField> fields, boolean additionalProperties) {
        this.typeName = Objects.requireNonNull(typeName);
        this.fields = List.copyOf(fields);
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String name() {
        return typeName.value();
    }
}
