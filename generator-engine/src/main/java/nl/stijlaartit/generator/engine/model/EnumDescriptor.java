package nl.stijlaartit.generator.engine.model;

import java.util.List;
import java.util.Objects;

public record EnumDescriptor(
        String name,
        List<String> enumValues,
        EnumValueType enumValueType,
        List<String> implementsTypes
) implements ModelDescriptor {
    public EnumDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(enumValues);
        Objects.requireNonNull(enumValueType);
        Objects.requireNonNull(implementsTypes);
        enumValues = List.copyOf(enumValues);
        implementsTypes = List.copyOf(implementsTypes);
    }
}
