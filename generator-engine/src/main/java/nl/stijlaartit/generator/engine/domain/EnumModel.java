package nl.stijlaartit.generator.engine.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnumModel implements ModelFile {
    private String name;
    private EnumValueType enumValueType;
    private final List<String> enumValues = new ArrayList<>();
    private final List<String> implementsTypes = new ArrayList<>();

    public EnumModel() {
    }

    public EnumModel(String name, List<String> enumValues, EnumValueType enumValueType,
                     List<String> implementsTypes) {
        this.name = Objects.requireNonNull(name);
        this.enumValueType = Objects.requireNonNull(enumValueType);
        if (enumValues != null) {
            this.enumValues.addAll(enumValues);
        }
        if (implementsTypes != null) {
            this.implementsTypes.addAll(implementsTypes);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public EnumValueType getEnumValueType() {
        return enumValueType;
    }

    public void setEnumValueType(EnumValueType enumValueType) {
        this.enumValueType = Objects.requireNonNull(enumValueType);
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public List<String> getImplementsTypes() {
        return implementsTypes;
    }

    @Override
    public List<String> getDependencies() {
        return List.copyOf(implementsTypes);
    }
}
