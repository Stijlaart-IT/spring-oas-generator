package nl.stijlaartit.spring.oas.generator.engine.domain;

import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record UnionModelFile(JavaTypeName typeName,
                             List<OneOfVariant> variants,
                             @Nullable
                             String discriminatorProperty) implements ModelFile {
    public UnionModelFile(JavaTypeName typeName, List<OneOfVariant> variants, @Nullable String discriminatorProperty) {
        this.typeName = Objects.requireNonNull(typeName);
        this.variants = List.copyOf(variants);
        this.discriminatorProperty = discriminatorProperty;
    }

    @Override
    public String name() {
        return typeName.value();
    }
}
