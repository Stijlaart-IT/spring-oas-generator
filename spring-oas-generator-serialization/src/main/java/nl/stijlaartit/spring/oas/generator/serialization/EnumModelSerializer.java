package nl.stijlaartit.spring.oas.generator.serialization;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.spring.oas.generator.domain.file.EnumModel;
import nl.stijlaartit.spring.oas.generator.domain.file.EnumValueType;
import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;

import javax.lang.model.element.Modifier;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EnumModelSerializer implements GenerationFileSerializer<EnumModel> {

    private static final ClassName JSON_PROPERTY =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");

    private final String modelsPackage;
    private final ImplementsByMapping implementsByModel;

    public EnumModelSerializer(String modelsPackage, ImplementsByMapping implementsByModel) {
        this.modelsPackage = modelsPackage;
        this.implementsByModel = implementsByModel;
    }

    @Override
    public SerializedFile serialize(EnumModel file) {
        return new SerializedFile.Ast(modelsPackage, toJavaFile(file));
    }

    @Override
    public boolean supports(GenerationFile generationFile) {
        return generationFile instanceof EnumModel;
    }

    JavaFile toJavaFile(EnumModel model) {
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(model.name())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GeneratedAnnotation.spec());

        for (JavaTypeName interfaceName : implementsByModel.parentInterfacesForName(model.typeName())) {
            enumBuilder.addSuperinterface(ClassName.get(modelsPackage, interfaceName.value()));
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
        if (value.isBlank()) {
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
