package nl.stijlaartit.generator.engine.domain;

import java.util.List;
import java.util.Objects;

public class UnionModelFile implements ModelFile {
    private final String name;
    private final String discriminatorProperty;
    private final List<OneOfVariant> variants;

    public UnionModelFile(String name, List<OneOfVariant> variants, String discriminatorProperty) {
        this.name = Objects.requireNonNull(name);
        this.variants = List.copyOf(variants);
        this.discriminatorProperty = discriminatorProperty;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDiscriminatorProperty() {
        return discriminatorProperty;
    }

    public List<OneOfVariant> getVariants() {
        return variants;
    }
}
