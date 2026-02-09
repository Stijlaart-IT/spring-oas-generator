package nl.stijlaartit.generation.model;

import nl.stijlaartit.generator.model.TypeDescriptor;

import java.util.List;
import java.util.Objects;

public record RecordDescriptor(
        String name,
        List<FieldDescriptor> fields,
        List<String> implementsTypes
) implements ModelDescriptor {
    public RecordDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(fields);
        Objects.requireNonNull(implementsTypes);
        fields = List.copyOf(fields);
        implementsTypes = List.copyOf(implementsTypes);
    }

    @Override
    public List<String> dependencies() {
        List<String> typeDependencies = fields.stream()
                .flatMap(field -> collectComplexTypes(field.type()).stream())
                .toList();
        return java.util.stream.Stream.concat(typeDependencies.stream(), implementsTypes.stream())
                .distinct()
                .filter(dep -> !dep.equals(name))
                .toList();
    }

    private static List<String> collectComplexTypes(TypeDescriptor type) {
        return switch (type) {
            case TypeDescriptor.SimpleType ignored -> List.of();
            case TypeDescriptor.ComplexType ct -> List.of(ct.modelName());
            case TypeDescriptor.ListType lt -> collectComplexTypes(lt.elementType());
            case TypeDescriptor.MapType mt -> collectComplexTypes(mt.valueType());
        };
    }
}
