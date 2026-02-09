package nl.stijlaartit.generation.model;

import java.util.List;
import java.util.Objects;

public record OneOfDescriptor(
        String name,
        List<String> variantModels,
        String discriminatorProperty
) implements ModelDescriptor {
    public OneOfDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(variantModels);
        variantModels = List.copyOf(variantModels);
    }

    @Override
    public List<String> dependencies() {
        return variantModels;
    }
}
