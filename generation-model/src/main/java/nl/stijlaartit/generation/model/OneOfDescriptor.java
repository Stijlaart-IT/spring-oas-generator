package nl.stijlaartit.generation.model;

import java.util.List;
import java.util.Objects;

public record OneOfDescriptor(
        String name,
        List<OneOfVariant> variants,
        String discriminatorProperty
) implements ModelDescriptor {
    public OneOfDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(variants);
        variants = List.copyOf(variants);
    }

    @Override
    public List<String> dependencies() {
        return variants.stream()
                .map(OneOfVariant::modelName)
                .toList();
    }

    public record OneOfVariant(String modelName, String discriminatorValue) {
        public OneOfVariant {
            Objects.requireNonNull(modelName);
        }
    }
}
