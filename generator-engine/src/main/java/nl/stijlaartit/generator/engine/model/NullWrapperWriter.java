package nl.stijlaartit.generator.engine.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeVariableName;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.engine.GeneratedAnnotation;
import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.GenerationFileSerializer;
import nl.stijlaartit.generator.engine.domain.NullWrapperFile;
import nl.stijlaartit.generator.engine.domain.SerializedFile;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public final class NullWrapperWriter implements GenerationFileSerializer<NullWrapperFile> {
    private static final ClassName JSON_VALUE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonValue");
    private static final ClassName JSON_DESERIALIZE =
            ClassName.get("tools.jackson.databind.annotation", "JsonDeserialize");
    private static final ClassName NULLABLE =
            ClassName.get("org.jspecify.annotations", "Nullable");
    private static final ClassName JSON_PARSER =
            ClassName.get("tools.jackson.core", "JsonParser");
    private static final ClassName JSON_TOKEN =
            ClassName.get("tools.jackson.core", "JsonToken");
    private static final ClassName BEAN_PROPERTY =
            ClassName.get("tools.jackson.databind", "BeanProperty");
    private static final ClassName DESERIALIZATION_CONTEXT =
            ClassName.get("tools.jackson.databind", "DeserializationContext");
    private static final ClassName JAVA_TYPE =
            ClassName.get("tools.jackson.databind", "JavaType");
    private static final ClassName VALUE_DESERIALIZER =
            ClassName.get("tools.jackson.databind", "ValueDeserializer");

    private final String modelsPackage;

    public NullWrapperWriter(String modelsPackage) {
        this.modelsPackage = modelsPackage;
    }

    @Override
    public SerializedFile serialize(NullWrapperFile file) {
        return new SerializedFile.Ast(modelsPackage, toJavaFile());
    }

    @Override
    public boolean supports(GenerationFile generationFile) {
        return generationFile instanceof NullWrapperFile;
    }

    public void write(Path outputDirectory) throws IOException {
        toJavaFile().writeTo(outputDirectory);
    }

    public JavaFile toJavaFile() {
        ClassName nullWrapper = ClassName.get(modelsPackage, "NullWrapper");
        ClassName deserializer = ClassName.get(modelsPackage, "NullWrapper", "Deserializer");
        TypeVariableName typeVar = TypeVariableName.get("T");

        MethodSpec recordConstructor = MethodSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder(typeVar, "value")
                        .addAnnotation(AnnotationSpec.builder(NULLABLE).build())
                        .addAnnotation(AnnotationSpec.builder(JSON_VALUE).build())
                        .build())
                .build();

        TypeSpec.Builder wrapper = TypeSpec.recordBuilder("NullWrapper")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVar)
                .addAnnotation(GeneratedAnnotation.spec())
                .addAnnotation(AnnotationSpec.builder(JSON_DESERIALIZE)
                        .addMember("using", "$T.class", deserializer)
                        .build())
                .recordConstructor(recordConstructor)
                .addType(deserializerType(nullWrapper));

        return JavaFile.builder(modelsPackage, wrapper.build())
                .indent("    ")
                .build();
    }

    private TypeSpec deserializerType(ClassName nullWrapper) {
        ParameterizedTypeName baseType = ParameterizedTypeName.get(VALUE_DESERIALIZER,
                ParameterizedTypeName.get(nullWrapper, ClassName.get("java.lang", "Object")));

        FieldSpec valueTypeField = FieldSpec.builder(JAVA_TYPE, "valueType", Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(NULLABLE).build())
                .build();

        MethodSpec constructorDefault = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.$N = null", valueTypeField)
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addParameter(JAVA_TYPE, "valueType")
                .addStatement("this.$N = $N", valueTypeField, "valueType")
                .build();

        MethodSpec getNullValue = MethodSpec.methodBuilder("getNullValue")
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(NULLABLE).build())
                .addModifiers(Modifier.PUBLIC)
                .returns(Object.class)
                .addParameter(DESERIALIZATION_CONTEXT, "ctxt")
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if ($N.getParser().currentToken() == $T.VALUE_NULL)", "ctxt", JSON_TOKEN)
                        .addStatement("return new $T<>(null)", nullWrapper)
                        .nextControlFlow("else")
                        .addStatement("return null")
                        .endControlFlow()
                        .build())
                .build();

        MethodSpec deserialize = MethodSpec.methodBuilder("deserialize")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(nullWrapper, ClassName.get("java.lang", "Object")))
                .addParameter(JSON_PARSER, "p")
                .addParameter(DESERIALIZATION_CONTEXT, "ctxt")
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if ($N.currentToken() == $T.VALUE_NULL)", "p", JSON_TOKEN)
                        .addStatement("return new $T<>(null)", nullWrapper)
                        .endControlFlow()
                        .addStatement("$T targetType = $N != null ? $N : $N.constructType(Object.class)",
                                JAVA_TYPE, valueTypeField, valueTypeField, "ctxt")
                        .addStatement("Object value = $N.readValue($N, targetType)", "ctxt", "p")
                        .addStatement("return new $T<>(value)", nullWrapper)
                        .build())
                .build();

        MethodSpec createContextual = MethodSpec.methodBuilder("createContextual")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(VALUE_DESERIALIZER)
                .addParameter(DESERIALIZATION_CONTEXT, "ctxt")
                .addParameter(BEAN_PROPERTY, "property")
                .beginControlFlow("if (property == null)")
                .addStatement("return this")
                .endControlFlow()
                .addStatement("$T wrapperType = property.getType()", JAVA_TYPE)
                .addStatement("$T innerType = wrapperType.containedType(0)", JAVA_TYPE)
                .addStatement("return new Deserializer(innerType)")
                .build();

        return TypeSpec.classBuilder("Deserializer")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .superclass(baseType)
                .addField(valueTypeField)
                .addMethod(constructorDefault)
                .addMethod(constructor)
                .addMethod(getNullValue)
                .addMethod(deserialize)
                .addMethod(createContextual)
                .build();
    }
}
