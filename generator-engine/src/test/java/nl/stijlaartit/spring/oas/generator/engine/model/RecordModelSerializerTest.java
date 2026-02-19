package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.engine.domain.FieldModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.engine.domain.RecordModel;
import nl.stijlaartit.spring.oas.generator.engine.domain.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.engine.GeneratedAnnotation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordModelSerializerTest {

    private final RecordModelSerializer writer = new RecordModelSerializer(
            "com.example.models",
            RecordModelWriterConfig.defaultConfig(),
            ImplementsByMapping.empty()
    );

    private RecordModelSerializer writerWithImplementsMap(ImplementsByMapping implementsByModel) {
        return new RecordModelSerializer(
                "com.example.models",
                RecordModelWriterConfig.defaultConfig(),
                implementsByModel
        );
    }

    @Test
    void generatesRecordWithSingleField() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("package com.example.models;"));
        assertGeneratedAnnotation(source);
        assertTrue(source.contains("record User("));
        assertTrue(source.contains("String name"));
    }

    @Test
    void generatesRecordWithMultipleFields() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false),
                new FieldModel("age", "age",
                        TypeDescriptor.simple("java.lang.Integer"), false, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("String name"));
        assertTrue(source.contains("@Nullable"));
        assertTrue(source.contains("Integer age"));
    }

    @Test
    void addsJsonPropertyWhenNamesDisagree() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("firstName", "first_name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(required = true"));
        assertTrue(source.contains("value = \"first_name\""));
        assertTrue(source.contains("import com.fasterxml.jackson.annotation.JsonProperty;"));
    }

    @Test
    void addsJsonPropertyWhenNamesMatch() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(required = true)"));
    }

    @Test
    void addsNullableForNullableProperty() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("age", "age",
                        TypeDescriptor.simple("java.lang.Integer"), false, true, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@Nullable"));
        assertTrue(source.contains("NullWrapper<Integer> age"));
        assertTrue(source.contains("import org.jspecify.annotations.Nullable;"));
    }

    @Test
    void addsJsonIncludeForRequiredPropertyOnly() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false),
                new FieldModel("nickname", "nickname",
                        TypeDescriptor.simple("java.lang.String"), false, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonInclude("));
        assertTrue(source.contains("Include.ALWAYS"));
        assertTrue(source.contains("Include.NON_NULL"));
        assertTrue(source.contains("@JsonProperty(required = false)"));
    }

    @Test
    void resolvesComplexTypeFromModelsPackage() {
        RecordModel model = new RecordModel("Owner", List.of(
                new FieldModel("pet", "pet",
                        TypeDescriptor.complex("Pet"), true, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Pet pet"));
    }

    private static void assertGeneratedAnnotation(String source) {
        assertTrue(source.contains("value = \"" + GeneratedAnnotation.VALUE + "\""));
        Pattern pattern = Pattern.compile(
                "@(?:javax\\.annotation\\.processing\\.)?Generated\\(\\s*value = \".+?\"\\s*,\\s*date = \"\\d{4}-\\d{2}-\\d{2}T[^\"]+\"\\s*\\)",
                Pattern.DOTALL
        );
        assertTrue(pattern.matcher(source).find());
    }

    @Test
    void resolvesListType() {
        RecordModel model = new RecordModel("Pet", List.of(
                new FieldModel("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), false, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<String> tags"));
    }

    @Test
    void resolvesMapType() {
        RecordModel model = new RecordModel("Config", List.of(
                new FieldModel("metadata", "metadata",
                        TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")), false, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Map<String, Integer> metadata"));
    }

    @Test
    void resolvesListOfComplexType() {
        RecordModel model = new RecordModel("Pet", List.of(
                new FieldModel("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.complex("Tag")), false, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<Tag> tags"));
    }

    @Test
    void generatesJsonValueRecordComponent() {
        RecordModel model = new RecordModel(
                "SessionResponse",
                List.of(new FieldModel(
                        "value",
                        "value",
                        TypeDescriptor.map(TypeDescriptor.simple("java.lang.Object")),
                        true,
                        true,
                        true
                ))
        );

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("record SessionResponse"));
        assertTrue(source.contains("@JsonValue"));
        assertTrue(source.contains("Map<String, Object> value"));
    }

    @Test
    void recordImplementsInterfaceWhenListedInUnionVariants() {
        RecordModel model = new RecordModel("TrackObject", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false)
        ));
        UnionModelFile union = new UnionModelFile(
                "QueueObjectCurrentlyPlaying",
                List.of(new OneOfVariant("TrackObject", "track")),
                "type"
        );

        String source = writerWithImplementsMap(ImplementsByMapping.create(List.of(model, union)))
                .toJavaFile(model).toString();

        assertTrue(source.contains("record TrackObject("));
        assertTrue(source.contains("implements QueueObjectCurrentlyPlaying"));
    }

    @Test
    void generatesBuilderByDefault() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("static Builder builder()"));
        assertTrue(source.contains("class Builder"));
    }

    @Test
    void builderStrictModeRequiresNonNullForNonNullableFields() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false),
                new FieldModel("age", "age",
                        TypeDescriptor.simple("java.lang.Integer"), false, false, false),
                new FieldModel("nickname", "nickname",
                        TypeDescriptor.simple("java.lang.String"), true, true, false),
                new FieldModel("bio", "bio",
                        TypeDescriptor.simple("java.lang.String"), false, true, false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Objects.requireNonNull(name"));
        assertFalse(source.contains("Objects.requireNonNull(age"));
        assertFalse(source.contains("Objects.requireNonNull(nickname"));
        assertFalse(source.contains("Objects.requireNonNull(bio"));
    }

    @Test
    void builderStrictModeCanBeDisabled() {
        RecordModelSerializer relaxedWriter = new RecordModelSerializer(
                "com.example.models",
                new RecordModelWriterConfig(RecordModelWriterConfig.BuilderMode.RELAXED),
                ImplementsByMapping.empty()
        );

        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true, false, false),
                new FieldModel("age", "age",
                        TypeDescriptor.simple("java.lang.Integer"), false, false, false)
        ));

        String source = relaxedWriter.toJavaFile(model).toString();

        assertFalse(source.contains("Objects.requireNonNull"));
    }
}
