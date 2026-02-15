package nl.stijlaartit.generator.engine.domain;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record UnionModelFile(String name,
                             List<OneOfVariant> variants,
                             @Nullable
                             String discriminatorProperty) implements ModelFile {
    public UnionModelFile(String name, List<OneOfVariant> variants, @Nullable String discriminatorProperty) {
        this.name = Objects.requireNonNull(name);
        this.variants = List.copyOf(variants);
        this.discriminatorProperty = discriminatorProperty;
    }
}
