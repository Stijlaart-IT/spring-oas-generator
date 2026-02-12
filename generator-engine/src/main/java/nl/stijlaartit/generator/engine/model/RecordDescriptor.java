package nl.stijlaartit.generator.engine.model;

import java.util.List;
import java.util.Objects;

public record RecordDescriptor(
        String name,
        List<FieldDescriptor> fields,
        List<String> implementsTypes
) implements ModelDescriptor {
    public RecordDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(fields);
        Objects.requireNonNull(implementsTypes);
        fields = List.copyOf(fields);
        implementsTypes = List.copyOf(implementsTypes);
    }
}
