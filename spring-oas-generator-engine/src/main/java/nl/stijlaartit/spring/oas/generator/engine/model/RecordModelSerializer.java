package nl.stijlaartit.spring.oas.generator.engine.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.spring.oas.generator.engine.GeneratedAnnotation;
import nl.stijlaartit.spring.oas.generator.engine.domain.FieldModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFileSerializer;
import nl.stijlaartit.spring.oas.generator.engine.domain.RecordModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.SerializedFile;

import javax.lang.model.element.Modifier;
import java.util.Objects;

public class RecordModelSerializer implements GenerationFileSerializer<RecordModel> {

    private static final ClassName JSON_PROPERTY =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty");
    private static final ClassName JSON_VALUE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonValue");
    private static final ClassName JSON_ANY_GETTER =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonAnyGetter");
    private static final ClassName JSON_ANY_SETTER =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonAnySetter");
    private static final ClassName JSON_INCLUDE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude");
    private static final ClassName JSON_INCLUDE_INCLUDE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude", "Include");
    private static final ClassName NULLABLE =
            ClassName.get("org.jspecify.annotations", "Nullable");
    private static final String NULL_WRAPPER_NAME = "NullWrapper";
    private static final String ADDITIONAL_PROPERTIES_NAME = "additionalProperties";

    private final TypeNameResolver typeNameResolver;
    private final String modelsPackage;
    private final RecordModelWriterConfig config;

    private final ImplementsByMapping implementsByModel;

    public RecordModelSerializer(String modelsPackage, RecordModelWriterConfig config, ImplementsByMapping implementsByModel) {
        this.modelsPackage = modelsPackage;
        this.typeNameResolver = new TypeNameResolver(modelsPackage);
        this.config = Objects.requireNonNull(config, "config");
        this.implementsByModel = implementsByModel;
    }

    @Override
    public SerializedFile serialize(RecordModel file) {
        final var javaFile = toJavaFile(file);
        return new SerializedFile.Ast(modelsPackage, javaFile);
    }

    @Override
    public boolean supports(GenerationFile generationFile) {
        return generationFile instanceof RecordModel;
    }

    JavaFile toJavaFile(RecordModel model) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

        for (FieldModel field : model.fields()) {
            var fieldType = resolveFieldType(field);
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(
                    fieldType,
                    field.name()
            );

            if (field.nullable() || !field.required()) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(NULLABLE).build());
            }

            AnnotationSpec.Builder jsonProperty = AnnotationSpec.builder(JSON_PROPERTY)
                    .addMember("required", "$L", field.required());
            if (!field.name().equals(field.jsonName())) {
                jsonProperty.addMember("value", "$S", field.jsonName());
            }
            paramBuilder.addAnnotation(jsonProperty.build());

            AnnotationSpec.Builder jsonInclude = AnnotationSpec.builder(JSON_INCLUDE);
            if (field.required()) {
                jsonInclude.addMember("value", "$T.ALWAYS", JSON_INCLUDE_INCLUDE);
            } else {
                jsonInclude.addMember("value", "$T.NON_NULL", JSON_INCLUDE_INCLUDE);
            }
            paramBuilder.addAnnotation(jsonInclude.build());
            if (field.jsonValue()) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(JSON_VALUE).build());
            }

            constructorBuilder.addParameter(paramBuilder.build());
        }

        if (model.additionalProperties()) {
            constructorBuilder.addParameter(additionalPropertiesParameter());
        }

        TypeSpec.Builder recordBuilder = TypeSpec.recordBuilder(model.name())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GeneratedAnnotation.spec())
                .recordConstructor(constructorBuilder.build());

        for (String interfaceName : implementsByModel.parentInterfacesForName(model.name())) {
            recordBuilder.addSuperinterface(ClassName.get(modelsPackage, interfaceName));
        }

        if (config.builderMode() != RecordModelWriterConfig.BuilderMode.DISABLED) {
            recordBuilder.addMethod(builderMethod(model));
            recordBuilder.addType(builderType(model));
        }

        return JavaFile.builder(modelsPackage, recordBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec builderMethod(RecordModel model) {
        ClassName builderType = ClassName.get(modelsPackage, model.name(), "Builder");
        return MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(builderType)
                .addStatement("return new $T()", builderType)
                .build();
    }

    private TypeSpec builderType(RecordModel model) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        ClassName builderType = ClassName.get(modelsPackage, model.name(), "Builder");
        for (FieldModel field : model.fields()) {
            builder.addField(FieldSpec.builder(
                            resolveFieldType(field),
                            field.name(),
                            Modifier.PRIVATE)
                    .addAnnotation(AnnotationSpec.builder(NULLABLE).build())
                    .build());
            builder.addMethod(builderSetter(builderType, field));
            if (isNullWrapperField(field)) {
                builder.addMethod(builderNullWrapperValueSetter(builderType, field));
            }
        }

        if (model.additionalProperties()) {
            builder.addField(FieldSpec.builder(
                            additionalPropertiesType(),
                            ADDITIONAL_PROPERTIES_NAME,
                            Modifier.PRIVATE)
                    .addAnnotation(AnnotationSpec.builder(NULLABLE).build())
                    .build());
            builder.addMethod(builderAdditionalPropertiesSetter(builderType));
        }

        builder.addMethod(builderBuildMethod(model));
        return builder.build();
    }

    private MethodSpec builderSetter(ClassName builderType, FieldModel field) {
        var typeName = resolveFieldType(field);
        ParameterSpec.Builder parameter = ParameterSpec.builder(typeName, field.name());
        if (!field.required() || field.nullable()) {
            parameter.addAnnotation(AnnotationSpec.builder(NULLABLE).build());
        }
        MethodSpec.Builder builder = MethodSpec.methodBuilder(field.name())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderType)
                .addParameter(parameter.build());
        return builder
                .addStatement("this.$N = $N", field.name(), field.name())
                .addStatement("return this")
                .build();
    }

    private MethodSpec builderNullWrapperValueSetter(ClassName builderType, FieldModel field) {
        var wrappedType = typeNameResolver.resolve(field.type());
        ParameterSpec.Builder parameter = ParameterSpec.builder(wrappedType, field.name())
                .addAnnotation(AnnotationSpec.builder(NULLABLE).build());
        return MethodSpec.methodBuilder(field.name())
                .addModifiers(Modifier.PUBLIC)
                .returns(builderType)
                .addParameter(parameter.build())
                .addStatement("this.$N = new $T<>($N)", field.name(),
                        ClassName.get(modelsPackage, NULL_WRAPPER_NAME), field.name())
                .addStatement("return this")
                .build();
    }

    private MethodSpec builderBuildMethod(RecordModel model) {
        ClassName recordType = ClassName.get(modelsPackage, model.name());
        CodeBlock.Builder args = CodeBlock.builder();
        boolean strictMode = config.builderMode() == RecordModelWriterConfig.BuilderMode.STRICT;
        for (int i = 0; i < model.fields().size(); i++) {
            FieldModel field = model.fields().get(i);
            if (i > 0) {
                args.add(", ");
            }
            boolean requireNonNull = strictMode && field.required() && !field.nullable();
            if (requireNonNull) {
                args.add("$T.requireNonNull($N, $S)", Objects.class, field.name(), field.name());
            } else {
                args.add("$N", field.name());
            }
        }
        if (model.additionalProperties()) {
            if (!model.fields().isEmpty()) {
                args.add(", ");
            }
            args.add("$N", ADDITIONAL_PROPERTIES_NAME);
        }
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(recordType)
                .addStatement("return new $T($L)", recordType, args.build())
                .build();
    }

    private com.palantir.javapoet.TypeName resolveFieldType(FieldModel field) {
        var baseType = typeNameResolver.resolve(field.type());
        if (isNullWrapperField(field)) {
            return com.palantir.javapoet.ParameterizedTypeName.get(
                    ClassName.get(modelsPackage, NULL_WRAPPER_NAME),
                    baseType
            );
        }
        return baseType;
    }

    private boolean isNullWrapperField(FieldModel field) {
        return !field.required() && field.nullable();
    }

    private com.palantir.javapoet.TypeName additionalPropertiesType() {
        return com.palantir.javapoet.ParameterizedTypeName.get(
                ClassName.get(java.util.Map.class),
                ClassName.get(String.class),
                ClassName.get(Object.class)
        );
    }

    private ParameterSpec additionalPropertiesParameter() {
        return ParameterSpec.builder(additionalPropertiesType(), ADDITIONAL_PROPERTIES_NAME)
                .addAnnotation(AnnotationSpec.builder(JSON_ANY_GETTER).build())
                .addAnnotation(AnnotationSpec.builder(JSON_ANY_SETTER).build())
                .addAnnotation(AnnotationSpec.builder(NULLABLE).build())
                .build();
    }

    private MethodSpec builderAdditionalPropertiesSetter(ClassName builderType) {
        return MethodSpec.methodBuilder(ADDITIONAL_PROPERTIES_NAME)
                .addModifiers(Modifier.PUBLIC)
                .returns(builderType)
                .addParameter(ParameterSpec.builder(additionalPropertiesType(), ADDITIONAL_PROPERTIES_NAME)
                        .addAnnotation(AnnotationSpec.builder(NULLABLE).build())
                        .build())
                .addStatement("this.$N = $N", ADDITIONAL_PROPERTIES_NAME, ADDITIONAL_PROPERTIES_NAME)
                .addStatement("return this")
                .build();
    }
}
