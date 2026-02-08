package nl.stijlaartit.generator.model;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
}
