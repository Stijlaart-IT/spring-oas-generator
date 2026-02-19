package nl.stijlaartit.spring.oas.generator.engine.model;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;

import java.util.List;
import java.util.Map;

public class TypeNameResolver {

    private final String modelsPackage;

    public TypeNameResolver(String modelsPackage) {
        this.modelsPackage = modelsPackage;
    }

    public TypeName resolve(TypeDescriptor type) {
        return switch (type) {
            case TypeDescriptor.SimpleType(String qualifiedName) -> resolveQualifiedName(qualifiedName);
            case TypeDescriptor.ComplexType(String modelName) -> ClassName.get(modelsPackage, modelName);
            case TypeDescriptor.ListType(TypeDescriptor elementType) ->
                    ParameterizedTypeName.get(ClassName.get(List.class), resolve(elementType));
            case TypeDescriptor.MapType(TypeDescriptor valueType) -> ParameterizedTypeName.get(
                    ClassName.get(Map.class), ClassName.get(String.class), resolve(valueType));
        };
    }

    private TypeName resolveQualifiedName(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        String packageName = qualifiedName.substring(0, lastDot);
        String simpleName = qualifiedName.substring(lastDot + 1);
        return ClassName.get(packageName, simpleName);
    }
}
