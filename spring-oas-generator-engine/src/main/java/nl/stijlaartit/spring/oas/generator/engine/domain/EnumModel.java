package nl.stijlaartit.spring.oas.generator.engine.domain;

import nl.stijlaartit.spring.oas.generator.engine.naming.JavaTypeName;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record EnumModel(JavaTypeName typeName,
                        EnumValueType enumValueType,
                        @Nullable List<String> enumValues) implements ModelFile {
    public EnumModel(JavaTypeName typeName, EnumValueType enumValueType, @Nullable List<String> enumValues) {
        this.typeName = Objects.requireNonNull(typeName);
        this.enumValueType = Objects.requireNonNull(enumValueType);
        this.enumValues = enumValues;
    }

    @Override
    public String name() {
        return typeName.value();
    }
}
