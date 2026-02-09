package nl.stijlaartit.generation.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.model.TypeNameResolver;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelWriter {

    private static final ClassName JSON_PROPERTY =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");

    private final TypeNameResolver typeNameResolver;
    private final String modelsPackage;

    public ModelWriter(String modelsPackage) {
        this.modelsPackage = modelsPackage;
        this.typeNameResolver = new TypeNameResolver(modelsPackage);
    }

    public void writeAll(List<ModelDescriptor> models, Path outputDirectory) throws IOException {
        for (ModelDescriptor model : models) {
            write(model, outputDirectory);
        }
    }

    public void write(ModelDescriptor model, Path outputDirectory) throws IOException {
        toJavaFile(model).writeTo(outputDirectory);
    }

    JavaFile toJavaFile(ModelDescriptor model) {
        if (model.isEnum()) {
            return toEnumJavaFile(model);
        }

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

        for (FieldDescriptor field : model.fields()) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                    typeNameResolver.resolve(field.type()),
                    field.name()
            );

            if (!field.name().equals(field.jsonName())) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(JSON_PROPERTY)
                                .addMember("value", "$S", field.jsonName())
                                .build()
                );
            }

            constructorBuilder.addParameter(paramBuilder.build());
        }

        TypeSpec recordSpec = TypeSpec.recordBuilder(model.name())
                .addModifiers(Modifier.PUBLIC)
                .recordConstructor(constructorBuilder.build())
                .build();

        return JavaFile.builder(modelsPackage, recordSpec)
                .indent("    ")
                .build();
    }

    private JavaFile toEnumJavaFile(ModelDescriptor model) {
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(model.name())
                .addModifiers(Modifier.PUBLIC);

        Map<String, Integer> usedNames = new HashMap<>();
        for (String value : model.enumValues()) {
            String baseName = toEnumConstantName(value);
            String name = uniqueName(baseName, usedNames);
            TypeSpec constantSpec = TypeSpec.anonymousClassBuilder("")
                    .addAnnotation(AnnotationSpec.builder(JSON_PROPERTY)
                            .addMember("value", "$S", value)
                            .build())
                    .build();
            enumBuilder.addEnumConstant(name, constantSpec);
        }

        return JavaFile.builder(modelsPackage, enumBuilder.build())
                .indent("    ")
                .build();
    }

    private static String toEnumConstantName(String value) {
        if (value == null || value.isBlank()) {
            return "EMPTY";
        }
        StringBuilder result = new StringBuilder();
        boolean previousUnderscore = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                result.append(Character.toUpperCase(c));
                previousUnderscore = false;
            } else if (!previousUnderscore) {
                result.append('_');
                previousUnderscore = true;
            }
        }
        String sanitized = result.toString();
        sanitized = trimUnderscores(sanitized);
        if (sanitized.isEmpty()) {
            sanitized = "VALUE";
        }
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "_" + sanitized;
        }
        return sanitized;
    }

    private static String uniqueName(String baseName, Map<String, Integer> usedNames) {
        int count = usedNames.getOrDefault(baseName, 0);
        usedNames.put(baseName, count + 1);
        return count == 0 ? baseName : baseName + "_" + (count + 1);
    }

    private static String trimUnderscores(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '_') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == '_') {
            end--;
        }
        return value.substring(start, end);
    }
}
