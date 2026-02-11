package nl.stijlaartit.generation.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.domain.EnumModel;
import nl.stijlaartit.generator.domain.EnumValueType;
import nl.stijlaartit.generator.domain.FieldModel;
import nl.stijlaartit.generator.domain.GenerationFileWriter;
import nl.stijlaartit.generator.domain.ModelFile;
import nl.stijlaartit.generator.domain.OneOfModel;
import nl.stijlaartit.generator.domain.OneOfVariant;
import nl.stijlaartit.generator.domain.RecordModel;
import nl.stijlaartit.generator.domain.WriteReport;
import nl.stijlaartit.generator.model.TypeNameResolver;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelWriter implements GenerationFileWriter<ModelFile> {

    private static final ClassName JSON_PROPERTY =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");
    private static final ClassName JSON_TYPE_INFO =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonTypeInfo");
    private static final ClassName JSON_SUB_TYPES =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes");
    private static final ClassName JSON_SUB_TYPES_TYPE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes", "Type");
    private static final ClassName NULLABLE =
            ClassName.get("org.jspecify.annotations", "Nullable");

    private final TypeNameResolver typeNameResolver;
    private final String modelsPackage;

    public ModelWriter(String modelsPackage) {
        this.modelsPackage = modelsPackage;
        this.typeNameResolver = new TypeNameResolver(modelsPackage);
    }

    @Override
    public WriteReport writeAll(List<ModelFile> models, Path outputDirectory) throws IOException {
        WriteReport report = new WriteReport();
        report.recordFile(writePackageInfo(outputDirectory));
        for (ModelFile model : models) {
            write(model, outputDirectory);
            report.recordFile(modelPath(outputDirectory, model.getName()));
        }
        return report;
    }

    public void write(ModelFile model, Path outputDirectory) throws IOException {
        toJavaFile(model).writeTo(outputDirectory);
    }

    JavaFile toJavaFile(ModelFile model) {
        return switch (model) {
            case EnumModel enumDescriptor -> toEnumJavaFile(enumDescriptor);
            case RecordModel recordDescriptor -> toRecordJavaFile(recordDescriptor);
            case OneOfModel oneOfDescriptor -> toOneOfJavaFile(oneOfDescriptor);
            default -> throw new IllegalArgumentException("Unsupported model type: " + model.getClass());
        };
    }

    private JavaFile toRecordJavaFile(RecordModel model) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

        for (FieldModel field : model.getFields()) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                    typeNameResolver.resolve(field.getType()),
                    field.getName()
            );

            if (!field.isRequired()) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(NULLABLE).build());
            }

            if (!field.getName().equals(field.getJsonName())) {
                paramBuilder.addAnnotation(
                        AnnotationSpec.builder(JSON_PROPERTY)
                                .addMember("value", "$S", field.getJsonName())
                                .build()
                );
            }

            constructorBuilder.addParameter(paramBuilder.build());
        }

        TypeSpec.Builder recordBuilder = TypeSpec.recordBuilder(model.getName())
                .addModifiers(Modifier.PUBLIC)
                .recordConstructor(constructorBuilder.build());

        for (String interfaceName : model.getImplementsTypes()) {
            recordBuilder.addSuperinterface(ClassName.get(modelsPackage, interfaceName));
        }

        TypeSpec recordSpec = recordBuilder.build();

        return JavaFile.builder(modelsPackage, recordSpec)
                .indent("    ")
                .build();
    }

    private JavaFile toEnumJavaFile(EnumModel model) {
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(model.getName())
                .addModifiers(Modifier.PUBLIC);

        for (String interfaceName : model.getImplementsTypes()) {
            enumBuilder.addSuperinterface(ClassName.get(modelsPackage, interfaceName));
        }

        Map<String, Integer> usedNames = new HashMap<>();
        for (String value : model.getEnumValues()) {
            String baseName = toEnumConstantName(value);
            String name = uniqueName(baseName, usedNames);
            if (model.getEnumValueType() == EnumValueType.STRING) {
                TypeSpec constantSpec = TypeSpec.anonymousClassBuilder("")
                        .addAnnotation(AnnotationSpec.builder(JSON_PROPERTY)
                                .addMember("value", "$S", value)
                                .build())
                        .build();
                enumBuilder.addEnumConstant(name, constantSpec);
            } else {
                enumBuilder.addEnumConstant(name,
                        TypeSpec.anonymousClassBuilder(enumValueCode(model.getEnumValueType(), value))
                                .build());
            }
        }

        if (model.getEnumValueType() != EnumValueType.STRING) {
            ClassName jsonValue = ClassName.get("com.fasterxml.jackson.annotation", "JsonValue");
            ClassName jsonCreator = ClassName.get("com.fasterxml.jackson.annotation", "JsonCreator");
            var valueType = enumValueTypeName(model.getEnumValueType());

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
                    .returns(ClassName.get(modelsPackage, model.getName()))
                    .addParameter(valueType, "value")
                    .beginControlFlow("for ($T candidate : values())", ClassName.get(modelsPackage, model.getName()))
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

    private JavaFile toOneOfJavaFile(OneOfModel model) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(model.getName())
                .addModifiers(Modifier.PUBLIC);

        if (model.getDiscriminatorProperty() != null && !model.getDiscriminatorProperty().isBlank()) {
            interfaceBuilder.addAnnotation(AnnotationSpec.builder(JSON_TYPE_INFO)
                    .addMember("use", "$T.Id.NAME", JSON_TYPE_INFO)
                    .addMember("include", "$T.As.PROPERTY", JSON_TYPE_INFO)
                    .addMember("property", "$S", model.getDiscriminatorProperty())
                    .addMember("visible", "$L", true)
                    .build());

            CodeBlock.Builder subTypes = CodeBlock.builder().add("{\n");
            var variants = model.getVariants();
            for (int i = 0; i < variants.size(); i++) {
                OneOfVariant variant = variants.get(i);
                AnnotationSpec.Builder typeBuilder = AnnotationSpec.builder(JSON_SUB_TYPES_TYPE)
                        .addMember("value", "$T.class", ClassName.get(modelsPackage, variant.getModelName()));
                String discriminatorValue = variant.getDiscriminatorValue();
                if (discriminatorValue != null && !discriminatorValue.isBlank()) {
                    typeBuilder.addMember("name", "$S", discriminatorValue);
                }
                subTypes.add("    $L", typeBuilder.build());
                if (i < variants.size() - 1) {
                    subTypes.add(",\n");
                } else {
                    subTypes.add("\n");
                }
            }
            subTypes.add("}");

            interfaceBuilder.addAnnotation(AnnotationSpec.builder(JSON_SUB_TYPES)
                    .addMember("value", "$L", subTypes.build())
                    .build());
        }

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

    private Path writePackageInfo(Path outputDirectory) throws IOException {
        Path packageDir = outputDirectory.resolve(modelsPackage.replace('.', '/'));
        Files.createDirectories(packageDir);
        Path packageInfo = packageDir.resolve("package-info.java");
        Files.writeString(packageInfo, packageInfoSource(modelsPackage));
        return packageInfo;
    }

    private Path modelPath(Path outputDirectory, String modelName) {
        return outputDirectory.resolve(modelsPackage.replace('.', '/'))
                .resolve(modelName + ".java");
    }

    private static String packageInfoSource(String packageName) {
        return "@NullMarked\n"
                + "package " + packageName + ";\n\n"
                + "import org.jspecify.annotations.NullMarked;\n";
    }
}
