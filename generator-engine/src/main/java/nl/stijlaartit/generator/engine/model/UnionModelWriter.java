package nl.stijlaartit.generator.engine.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import nl.stijlaartit.generator.engine.GeneratedAnnotation;
import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.UnionModelFile;

import javax.lang.model.element.Modifier;

class UnionModelWriter {

    private static final ClassName JSON_TYPE_INFO =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonTypeInfo");
    private static final ClassName JSON_SUB_TYPES =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes");
    private static final ClassName JSON_SUB_TYPES_TYPE =
            ClassName.get("com.fasterxml.jackson.annotation", "JsonSubTypes", "Type");

    private final String modelsPackage;

    UnionModelWriter(String modelsPackage) {
        this.modelsPackage = modelsPackage;
    }

    JavaFile toJavaFile(UnionModelFile model) {
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(model.name())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(GeneratedAnnotation.spec());

        if (model.discriminatorProperty() != null && !model.discriminatorProperty().isBlank()) {
            interfaceBuilder.addAnnotation(AnnotationSpec.builder(JSON_TYPE_INFO)
                    .addMember("use", "$T.Id.NAME", JSON_TYPE_INFO)
                    .addMember("include", "$T.As.PROPERTY", JSON_TYPE_INFO)
                    .addMember("property", "$S", model.discriminatorProperty())
                    .addMember("visible", "$L", true)
                    .build());

            CodeBlock.Builder subTypes = CodeBlock.builder().add("{\n");
            var variants = model.variants();
            for (int i = 0; i < variants.size(); i++) {
                OneOfVariant variant = variants.get(i);
                AnnotationSpec.Builder typeBuilder = AnnotationSpec.builder(JSON_SUB_TYPES_TYPE)
                        .addMember("value", "$T.class", ClassName.get(modelsPackage, variant.modelName()));
                String discriminatorValue = variant.discriminatorValue();
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
}
