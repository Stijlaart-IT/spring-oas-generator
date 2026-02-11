package nl.stijlaartit.generator.engine.model;

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
        if (type instanceof TypeDescriptor.SimpleType st) {
            return resolveQualifiedName(st.qualifiedName());
        } else if (type instanceof TypeDescriptor.ComplexType ct) {
            return ClassName.get(modelsPackage, ct.modelName());
        } else if (type instanceof TypeDescriptor.ListType lt) {
            return ParameterizedTypeName.get(ClassName.get(List.class), resolve(lt.elementType()));
        } else if (type instanceof TypeDescriptor.MapType mt) {
            return ParameterizedTypeName.get(
                    ClassName.get(Map.class), ClassName.get(String.class), resolve(mt.valueType()));
        }
        throw new IllegalArgumentException("Unknown TypeDescriptor: " + type);
    }

    private TypeName resolveQualifiedName(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        String packageName = qualifiedName.substring(0, lastDot);
        String simpleName = qualifiedName.substring(lastDot + 1);
        return ClassName.get(packageName, simpleName);
    }
}
