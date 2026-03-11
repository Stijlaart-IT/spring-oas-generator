package nl.stijlaartit.spring.oas.generator.serialization;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;
import nl.stijlaartit.spring.oas.generator.domain.file.SpringConfigFile;

import javax.lang.model.element.Modifier;

public class SpringConfigSerializer implements GenerationFileSerializer<SpringConfigFile> {

    private static final ClassName CONFIGURATION =
            ClassName.get("org.springframework.context.annotation", "Configuration");
    private static final ClassName IMPORT_HTTP_SERVICES =
            ClassName.get("org.springframework.web.service.registry", "ImportHttpServices");

    @Override
    public SerializedFile serialize(SpringConfigFile file) {
        return new SerializedFile.Ast(file.packageName(), toJavaFile(file));
    }

    @Override
    public boolean supports(GenerationFile generationFile) {
        return generationFile instanceof SpringConfigFile;
    }

    JavaFile toJavaFile(SpringConfigFile file) {
        AnnotationSpec.Builder importHttpServicesBuilder = AnnotationSpec.builder(IMPORT_HTTP_SERVICES)
                .addMember("group", "$S", file.serviceGroupName());

        String joinedTypes = file.apiTypeNames().stream()
                .map(typeName -> "$T.class")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        Object[] typeArgs = file.apiTypeNames().stream()
                .map(typeName -> ClassName.get(file.apiPackage(), typeName))
                .toArray();
        importHttpServicesBuilder.addMember("types", "{" + joinedTypes + "}", typeArgs);

        TypeSpec typeSpec = TypeSpec.classBuilder(file.name())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GeneratedAnnotation.spec())
                .addAnnotation(CONFIGURATION)
                .addAnnotation(importHttpServicesBuilder.build())
                .build();

        return JavaFile.builder(file.packageName(), typeSpec)
                .indent("    ")
                .build();
    }
}
