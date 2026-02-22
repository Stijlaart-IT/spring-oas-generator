package nl.stijlaartit.spring.oas.generator.serialization;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;

public class TypeNameResolver {


    public static TypeName resolve(TypeDescriptor type) {
        ClassName base = ClassName.get(type.packageName(), type.modelName().value());
        if (type.generics().isEmpty()) {
            return base;
        } else {
            TypeName[] generics = type.generics().stream().map(TypeNameResolver::resolve).toArray(TypeName[]::new);
            return ParameterizedTypeName.get(base, generics);
        }
    }
}
