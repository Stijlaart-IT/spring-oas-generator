package nl.stijlaartit.spring.oas.generator.domain.file;

import java.util.List;

public record TypeDescriptor(String packageName, JavaTypeName modelName,
                             List<TypeDescriptor> generics) {

    public static TypeDescriptor qualified(String packageName, JavaTypeName qualifiedName) {
        return new TypeDescriptor(packageName, qualifiedName, List.of());
    }

    public static TypeDescriptor list(TypeDescriptor elementType) {
        return new TypeDescriptor("java.util", new JavaTypeName.Reserved("List"), List.of(elementType));
    }

    public static TypeDescriptor map(TypeDescriptor valueType) {
        return new TypeDescriptor("java.util", new JavaTypeName.Generated("Map"), List.of(
                TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")),
                valueType
        ));
    }
}
