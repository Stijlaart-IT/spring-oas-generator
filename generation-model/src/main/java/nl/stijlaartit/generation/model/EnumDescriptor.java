package nl.stijlaartit.generation.model;

import java.util.List;
import java.util.Objects;

public record EnumDescriptor(
        String name,
        List<String> enumValues,
        EnumValueType enumValueType
) implements ModelDescriptor {
    public EnumDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(enumValues);
        Objects.requireNonNull(enumValueType);
        enumValues = List.copyOf(enumValues);
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }
}
