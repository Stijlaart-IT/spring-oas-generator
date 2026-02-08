package nl.stijlaartit.generator.model;

import java.util.List;
import java.util.Objects;

public record ModelDescriptor(
        String name,
        List<FieldDescriptor> fields
) {
    public ModelDescriptor {
        Objects.requireNonNull(name);
        fields = List.copyOf(fields);
    }

    public List<String> dependencies() {
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
