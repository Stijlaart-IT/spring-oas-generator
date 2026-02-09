package nl.stijlaartit.generation.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.model.TypeNameResolver;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        return switch (model) {
            case EnumDescriptor enumDescriptor -> toEnumJavaFile(enumDescriptor);
            case RecordDescriptor recordDescriptor -> toRecordJavaFile(recordDescriptor);
            case OneOfDescriptor oneOfDescriptor -> toOneOfJavaFile(oneOfDescriptor);
        };
    }

    private JavaFile toRecordJavaFile(RecordDescriptor model) {
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

        TypeSpec.Builder recordBuilder = TypeSpec.recordBuilder(model.name())
                .addModifiers(Modifier.PUBLIC)
                .recordConstructor(constructorBuilder.build());

        for (String interfaceName : model.implementsTypes()) {
            recordBuilder.addSuperinterface(ClassName.get(modelsPackage, interfaceName));
        }

        TypeSpec recordSpec = recordBuilder.build();

        return JavaFile.builder(modelsPackage, recordSpec)
                .indent("    ")
                .build();
    }

    private JavaFile toEnumJavaFile(EnumDescriptor model) {
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(model.name())
                .addModifiers(Modifier.PUBLIC);

        for (String interfaceName : model.implementsTypes()) {
            enumBuilder.addSuperinterface(ClassName.get(modelsPackage, interfaceName));
        }

        Map<String, Integer> usedNames = new HashMap<>();
        for (String value : model.enumValues()) {
            String baseName = toEnumConstantName(value);
            String name = uniqueName(baseName, usedNames);
            if (model.enumValueType() == EnumValueType.STRING) {
                TypeSpec constantSpec = TypeSpec.anonymousClassBuilder("")
                        .addAnnotation(AnnotationSpec.builder(JSON_PROPERTY)
                                .addMember("value", "$S", value)
                                .build())
                        .build();
                enumBuilder.addEnumConstant(name, constantSpec);
            } else {
                enumBuilder.addEnumConstant(name,
                        TypeSpec.anonymousClassBuilder(enumValueCode(model.enumValueType(), value))
                                .build());
            }
        }

        if (model.enumValueType() != EnumValueType.STRING) {
            ClassName jsonValue = ClassName.get("com.fasterxml.jackson.annotation", "JsonValue");
            ClassName jsonCreator = ClassName.get("com.fasterxml.jackson.annotation", "JsonCreator");
            var valueType = enumValueTypeName(model.enumValueType());

            enumBuilder.addField(FieldSpec.builder(valueType, "value", Modifier.PRIVATE, Modifier.FINAL)
                    .build());

            enumBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addParameter(valueType, "value")
                    .addStatement("this.value = value")
                    .build());

            enumBuilder.addMethod(MethodSpec.methodBuilder("value")
                    .addAnnotation(jsonValue)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(valueType)
                    .addStatement("return value")
                    .build());

            enumBuilder.addMethod(MethodSpec.methodBuilder("fromValue")
                    .addAnnotation(jsonCreator)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.get(modelsPackage, model.name()))
                    .addParameter(valueType, "value")
                    .beginControlFlow("for ($T candidate : values())", ClassName.get(modelsPackage, model.name()))
                    .beginControlFlow("if ($T.equals(candidate.value, value))", Objects.class)
                    .addStatement("return candidate")
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("throw new $T(\"Unexpected value '\" + value + \"'\")",
                            IllegalArgumentException.class)
                    .build());
        }

        return JavaFile.builder(modelsPackage, enumBuilder.build())
                .indent("    ")
                .build();
    }

    private JavaFile toOneOfJavaFile(OneOfDescriptor model) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(model.name())
                .addModifiers(Modifier.PUBLIC);

        return JavaFile.builder(modelsPackage, interfaceBuilder.build())
                .indent("    ")
                .build();
    }

    private static CodeBlock enumValueCode(EnumValueType enumValueType, String value) {
        return switch (enumValueType) {
            case INTEGER, BOOLEAN -> CodeBlock.of("$L", value);
            case NUMBER -> CodeBlock.of("new $T($S)", BigDecimal.class, value);
            case STRING -> throw new IllegalStateException("String enum values are handled separately");
        };
    }

    private static com.palantir.javapoet.TypeName enumValueTypeName(EnumValueType enumValueType) {
        return switch (enumValueType) {
            case INTEGER -> ClassName.get(Integer.class);
            case NUMBER -> ClassName.get(BigDecimal.class);
            case BOOLEAN -> ClassName.get(Boolean.class);
            case STRING -> ClassName.get(String.class);
        };
    }

    private static String toEnumConstantName(String value) {
        if (value == null || value.isBlank()) {
            return "EMPTY";
        }
        boolean negative = value.startsWith("-");
        String normalized = negative ? value.substring(1) : value;
        StringBuilder result = new StringBuilder();
        boolean previousUnderscore = false;
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
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
        return negative ? "NEGATIVE" + sanitized : sanitized;
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
