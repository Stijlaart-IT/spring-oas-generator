package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;

import java.util.ArrayList;
import java.util.List;

public final class TypeDependencies {
    private TypeDependencies() {
    }

    public static List<String> collectComplexTypes(TypeDescriptor type) {
        return switch (type) {
            case TypeDescriptor.SimpleType ignored -> List.of();
            case TypeDescriptor.ComplexType ct -> List.of(ct.modelName());
            case TypeDescriptor.ListType lt -> collectComplexTypes(lt.elementType());
            case TypeDescriptor.MapType mt -> collectComplexTypes(mt.valueType());
        };
    }

    public static List<String> collectQualifiedTypes(TypeDescriptor type) {
        List<String> dependencies = new ArrayList<>();
        collectQualifiedTypes(type, dependencies);
        return List.copyOf(dependencies);
    }

    private static void collectQualifiedTypes(TypeDescriptor type, List<String> sink) {
        switch (type) {
            case TypeDescriptor.SimpleType st -> sink.add(st.qualifiedName());
            case TypeDescriptor.ComplexType ct -> sink.add(ct.modelName());
            case TypeDescriptor.ListType lt -> {
                sink.add("java.util.List");
                collectQualifiedTypes(lt.elementType(), sink);
            }
            case TypeDescriptor.MapType mt -> {
                sink.add("java.util.Map");
                collectQualifiedTypes(mt.valueType(), sink);
            }
        }
    }
}
