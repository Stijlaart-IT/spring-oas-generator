package nl.stijlaartit.generator.engine.domain;

import java.util.Objects;

public class OneOfVariant {
    private final String modelName;
    private final String discriminatorValue;

    public OneOfVariant(String modelName, String discriminatorValue) {
        this.modelName = Objects.requireNonNull(modelName);
        this.discriminatorValue = discriminatorValue;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDiscriminatorValue() {
        return discriminatorValue;
    }
}
