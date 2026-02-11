package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecordModel implements ModelFile {
    private String name;
    private final List<FieldModel> fields = new ArrayList<>();
    private final List<String> implementsTypes = new ArrayList<>();

    public RecordModel() {
    }

    public RecordModel(String name, List<FieldModel> fields, List<String> implementsTypes) {
        this.name = Objects.requireNonNull(name);
        if (fields != null) {
            this.fields.addAll(fields);
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

    public List<FieldModel> getFields() {
        return fields;
    }

    public List<String> getImplementsTypes() {
        return implementsTypes;
    }

    @Override
    public List<String> getDependencies() {
        List<String> typeDependencies = fields.stream()
                .flatMap(field -> TypeDependencies.collectComplexTypes(field.getType()).stream())
                .toList();
        return java.util.stream.Stream.concat(typeDependencies.stream(), implementsTypes.stream())
                .distinct()
                .filter(dep -> !dep.equals(name))
                .toList();
    }
}
