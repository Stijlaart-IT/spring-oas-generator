package nl.stijlaartit.spring.oas.generator.domain.file;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record OneOfVariant(JavaTypeName modelName,
                           @Nullable
                           String discriminatorValue) {
    public OneOfVariant(JavaTypeName modelName, @Nullable String discriminatorValue) {
        this.modelName = Objects.requireNonNull(modelName);
        this.discriminatorValue = discriminatorValue;
    }
}
