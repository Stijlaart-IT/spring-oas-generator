package nl.stijlaartit.generator.engine.domain;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record OneOfVariant(String modelName,
                           @Nullable
                           String discriminatorValue) {
    public OneOfVariant(String modelName, @Nullable String discriminatorValue) {
        this.modelName = Objects.requireNonNull(modelName);
        this.discriminatorValue = discriminatorValue;
    }
}
