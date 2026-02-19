package nl.stijlaartit.spring.oas.generator.engine.domain;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnumModel implements ModelFile {
    private final String name;
    private final EnumValueType enumValueType;
    private final List<String> enumValues = new ArrayList<>();

    public EnumModel(String name, @Nullable List<String> enumValues, EnumValueType enumValueType) {
        this.name = Objects.requireNonNull(name);
        this.enumValueType = Objects.requireNonNull(enumValueType);
        if (enumValues != null) {
            this.enumValues.addAll(enumValues);
        }
    }

    @Override
    public String name() {
        return name;
    }

    public EnumValueType getEnumValueType() {
        return enumValueType;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }
}
