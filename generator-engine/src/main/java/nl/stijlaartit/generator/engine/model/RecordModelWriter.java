package nl.stijlaartit.generator.engine.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.engine.domain.FieldModel;
import nl.stijlaartit.generator.engine.domain.RecordModel;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class RecordModelWriter {

    private static final ClassName JSON_PROPERTY =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");
    private static final ClassName JSON_VALUE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonValue");
    private static final ClassName JSON_INCLUDE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude");
    private static final ClassName JSON_INCLUDE_INCLUDE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude", "Include");
    private static final ClassName NULLABLE =
            ClassName.get("org.jspecify.annotations", "Nullable");

    private final TypeNameResolver typeNameResolver;
    private final String modelsPackage;
    private final RecordModelWriterConfig config;

    RecordModelWriter(String modelsPackage, RecordModelWriterConfig config) {
        this.modelsPackage = modelsPackage;
        this.typeNameResolver = new TypeNameResolver(modelsPackage);
        this.config = Objects.requireNonNull(config, "config");
    }

    JavaFile toJavaFile(RecordModel model, Map<String, List<String>> implementsByModel) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

        for (FieldModel field : model.getFields()) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                    typeNameResolver.resolve(field.getType()),
                    field.getName()
            );

            if (field.isNullable()) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(NULLABLE).build());
            }

            AnnotationSpec.Builder jsonProperty = AnnotationSpec.builder(JSON_PROPERTY)
                    .addMember("required", "$L", field.isRequired());
            if (!field.getName().equals(field.getJsonName())) {
                jsonProperty.addMember("value", "$S", field.getJsonName());
            }
            paramBuilder.addAnnotation(jsonProperty.build());

            if (field.isRequired()) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(JSON_INCLUDE)
                        .addMember("value", "$T.ALWAYS", JSON_INCLUDE_INCLUDE)
                        .build());
            }
            if (field.isJsonValue()) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(JSON_VALUE).build());
            }

            constructorBuilder.addParameter(paramBuilder.build());
        }

        TypeSpec.Builder recordBuilder = TypeSpec.recordBuilder(model.getName())
                .addModifiers(Modifier.PUBLIC)
                .recordConstructor(constructorBuilder.build());

        for (String interfaceName : implementsByModel.getOrDefault(model.getName(), List.of())) {
            recordBuilder.addSuperinterface(ClassName.get(modelsPackage, interfaceName));
        }

        if (config.generateBuilders()) {
            recordBuilder.addMethod(builderMethod(model));
            recordBuilder.addType(builderType(model));
        }

        return JavaFile.builder(modelsPackage, recordBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec builderMethod(RecordModel model) {
        ClassName builderType = ClassName.get(modelsPackage, model.getName(), "Builder");
        return MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(builderType)
                .addStatement("return new $T()", builderType)
                .build();
    }

    private TypeSpec builderType(RecordModel model) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        ClassName builderType = ClassName.get(modelsPackage, model.getName(), "Builder");
        for (FieldModel field : model.getFields()) {
            builder.addField(FieldSpec.builder(
                            typeNameResolver.resolve(field.getType()),
                            field.getName(),
                            Modifier.PRIVATE)
                    .build());
            builder.addMethod(builderSetter(builderType, field));
        }

        builder.addMethod(builderBuildMethod(model));
        return builder.build();
    }

    private MethodSpec builderSetter(ClassName builderType, FieldModel field) {
        var typeName = typeNameResolver.resolve(field.getType());
        return MethodSpec.methodBuilder(field.getName())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderType)
                .addParameter(typeName, field.getName())
                .addStatement("this.$N = $N", field.getName(), field.getName())
                .addStatement("return this")
                .build();
    }

    private MethodSpec builderBuildMethod(RecordModel model) {
        ClassName recordType = ClassName.get(modelsPackage, model.getName());
        CodeBlock.Builder args = CodeBlock.builder();
        boolean strictMode = !config.disableBuilderStrictMode();
        for (int i = 0; i < model.getFields().size(); i++) {
            FieldModel field = model.getFields().get(i);
            if (i > 0) {
                args.add(", ");
            }
            boolean requireNonNull = strictMode && !field.isNullable();
            if (requireNonNull) {
                args.add("$T.requireNonNull($N, $S)", Objects.class, field.getName(), field.getName());
            } else {
                args.add("$N", field.getName());
            }
        }
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(recordType)
                .addStatement("return new $T($L)", recordType, args.build())
                .build();
    }
}
