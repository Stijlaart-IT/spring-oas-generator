package nl.stijlaartit.generation.model;

import nl.stijlaartit.generator.model.TypeDescriptor;

import java.util.List;
import java.util.Objects;

public record ModelDescriptor(
        String name,
        List<FieldDescriptor> fields,
        List<String> enumValues
) {
    public ModelDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(fields);
        Objects.requireNonNull(enumValues);
        fields = List.copyOf(fields);
        enumValues = List.copyOf(enumValues);
        if (!enumValues.isEmpty() && !fields.isEmpty()) {
            throw new IllegalArgumentException("Model cannot have both fields and enum values");
        }
    }

    public static ModelDescriptor record(String name, List<FieldDescriptor> fields) {
        return new ModelDescriptor(name, fields, List.of());
    }

    public static ModelDescriptor enumModel(String name, List<String> enumValues) {
        return new ModelDescriptor(name, List.of(), enumValues);
    }

    public boolean isEnum() {
        return !enumValues.isEmpty();
    }

    public List<String> dependencies() {
        if (isEnum()) {
            return List.of();
        }
        return fields.stream()
                .flatMap(field -> collectComplexTypes(field.type()).stream())
                .distinct()
                .filter(dep -> !dep.equals(name))
                .toList();
    }

    private static List<String> collectComplexTypes(TypeDescriptor type) {
        if (type instanceof TypeDescriptor.SimpleType) {
            return List.of();
        } else if (type instanceof TypeDescriptor.ComplexType ct) {
            return List.of(ct.modelName());
        } else if (type instanceof TypeDescriptor.ListType lt) {
            return collectComplexTypes(lt.elementType());
        } else if (type instanceof TypeDescriptor.MapType mt) {
            return collectComplexTypes(mt.valueType());
        }
        return List.of();
    }
}
