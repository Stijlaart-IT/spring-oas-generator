package nl.stijlaartit.generator.engine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OneOfModel implements ModelFile {
    private String name;
    private String discriminatorProperty;
    private final List<OneOfVariant> variants = new ArrayList<>();

    public OneOfModel() {
    }

    public OneOfModel(String name, List<OneOfVariant> variants, String discriminatorProperty) {
        this.name = Objects.requireNonNull(name);
        if (variants != null) {
            this.variants.addAll(variants);
        }
        this.discriminatorProperty = discriminatorProperty;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getDiscriminatorProperty() {
        return discriminatorProperty;
    }

    public void setDiscriminatorProperty(String discriminatorProperty) {
        this.discriminatorProperty = discriminatorProperty;
    }

    public List<OneOfVariant> getVariants() {
        return variants;
    }

    @Override
    public List<String> getDependencies() {
        return variants.stream()
                .map(OneOfVariant::getModelName)
                .toList();
    }
}
