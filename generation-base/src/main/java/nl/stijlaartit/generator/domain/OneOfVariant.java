package nl.stijlaartit.generator.domain;

import java.util.Objects;

public class OneOfVariant {
    private String modelName;
    private String discriminatorValue;

    public OneOfVariant() {
    }

    public OneOfVariant(String modelName, String discriminatorValue) {
        this.modelName = Objects.requireNonNull(modelName);
        this.discriminatorValue = discriminatorValue;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = Objects.requireNonNull(modelName);
    }

    public String getDiscriminatorValue() {
        return discriminatorValue;
    }

    public void setDiscriminatorValue(String discriminatorValue) {
        this.discriminatorValue = discriminatorValue;
    }
}
