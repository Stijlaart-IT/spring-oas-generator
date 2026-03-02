package nl.stijlaartit.spring.oas.generator.serialization;

import nl.stijlaartit.spring.oas.generator.domain.file.TypeDescriptor;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordField;
import nl.stijlaartit.spring.oas.generator.domain.file.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordModel;
import nl.stijlaartit.spring.oas.generator.domain.file.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaParameterName;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
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
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("package com.example.models;"));
        assertGeneratedAnnotation(source);
        assertTrue(source.contains("record User("));
        assertTrue(source.contains("String name"));
    }

    @Test
    void generatesRecordWithMultipleFields() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false),
                new RecordField(new JavaParameterName("age"), "age",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer")), false, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("String name"));
        assertTrue(source.contains("@Nullable"));
        assertTrue(source.contains("Integer age"));
    }

    @Test
    void addsJsonPropertyWhenNamesDisagree() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("firstName"), "first_name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(required = true"));
        assertTrue(source.contains("value = \"first_name\""));
        assertTrue(source.contains("import com.fasterxml.jackson.annotation.JsonProperty;"));
    }

    @Test
    void addsJsonPropertyWhenNamesMatch() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(required = true)"));
    }

    @Test
    void disableJacksonRequiredForcesJsonPropertyRequiredFalse() {
        RecordModelSerializer serializer = new RecordModelSerializer(
                "com.example.models",
                new RecordModelWriterConfig(BuilderMode.STRICT, true),
                ImplementsByMapping.empty()
        );
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false)
        ), false);

        String source = serializer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(required = false)"));
        assertFalse(source.contains("@JsonProperty(required = true)"));
    }

    @Test
    void addsNullableForNullableProperty() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("age"), "age",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer")), false, true, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@Nullable"));
        assertTrue(source.contains("NullWrapper<Integer> age"));
        assertTrue(source.contains("import org.jspecify.annotations.Nullable;"));
    }

    @Test
    void addsJsonIncludeForRequiredPropertyOnly() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false),
                new RecordField(new JavaParameterName("nickname"), "nickname",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), false, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonInclude("));
        assertTrue(source.contains("Include.ALWAYS"));
        assertTrue(source.contains("Include.NON_NULL"));
        assertTrue(source.contains("@JsonProperty(required = false)"));
    }

    @Test
    void resolvesComplexTypeFromModelsPackage() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("Owner"), List.of(
                new RecordField(new JavaParameterName("pet"), "pet",
                        TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Pet")), true, false, false)
        ), false);

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
        RecordModel model = new RecordModel(new JavaTypeName.Generated("Pet"), List.of(
                new RecordField(new JavaParameterName("tags"), "tags",
                        TypeDescriptor.list(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String"))), false, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<String> tags"));
    }

    @Test
    void resolvesMapType() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("Config"), List.of(
                new RecordField(new JavaParameterName("metadata"), "metadata",
                        TypeDescriptor.map(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer"))), false, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Map<String, Integer> metadata"));
    }

    @Test
    void resolvesListOfComplexType() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("Pet"), List.of(
                new RecordField(new JavaParameterName("tags"), "tags",
                        TypeDescriptor.list(TypeDescriptor.qualified("com.example.models", new JavaTypeName.Generated("Tag"))), false, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<Tag> tags"));
    }

    @Test
    void addsAdditionalPropertiesFieldWhenEnabled() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("Config"), List.of(), true);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonAnyGetter"));
        assertTrue(source.contains("@JsonAnySetter"));
        assertTrue(source.contains("Map<String, Object> additionalProperties"));
        assertTrue(source.contains("import com.fasterxml.jackson.annotation.JsonAnyGetter;"));
        assertTrue(source.contains("import com.fasterxml.jackson.annotation.JsonAnySetter;"));
    }

    @Test
    void builderIncludesAdditionalPropertiesWhenEnabled() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("Config"), List.of(), true);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("class Builder"));
        assertTrue(source.contains("Map<String, Object> additionalProperties"));
        assertTrue(source.contains("Builder additionalProperties("));
        assertTrue(source.contains("return new Config(additionalProperties)"));
    }

    @Test
    void generatesJsonValueRecordComponent() {
        RecordModel model = new RecordModel(
                new JavaTypeName.Generated("SessionResponse"),
                List.of(new RecordField(
                        new JavaParameterName("value"),
                        "value",
                        TypeDescriptor.map(TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Object"))),
                        true,
                        true,
                        true
                )),
                false
        );

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("record SessionResponse"));
        assertTrue(source.contains("@JsonValue"));
        assertTrue(source.contains("Map<String, Object> value"));
    }

    @Test
    void recordImplementsInterfaceWhenListedInUnionVariants() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("TrackObject"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false)
        ), false);
        UnionModelFile union = new UnionModelFile(
                new JavaTypeName.Generated("QueueObjectCurrentlyPlaying"),
                List.of(new OneOfVariant(new JavaTypeName.Generated("TrackObject"), "track")),
                "type"
        );

        String source = writerWithImplementsMap(ImplementsByMapping.create(List.of(model, union)))
                .toJavaFile(model).toString();

        assertTrue(source.contains("record TrackObject("));
        assertTrue(source.contains("implements QueueObjectCurrentlyPlaying"));
    }

    @Test
    void generatesBuilderByDefault() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false)
        ), false);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("static Builder builder()"));
        assertTrue(source.contains("class Builder"));
    }

    @Test
    void builderStrictModeRequiresNonNullForNonNullableFields() {
        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false),
                new RecordField(new JavaParameterName("age"), "age",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer")), false, false, false),
                new RecordField(new JavaParameterName("nickname"), "nickname",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, true, false),
                new RecordField(new JavaParameterName("bio"), "bio",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), false, true, false)
        ), false);

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
                new RecordModelWriterConfig(BuilderMode.RELAXED),
                ImplementsByMapping.empty()
        );

        RecordModel model = new RecordModel(new JavaTypeName.Generated("User"), List.of(
                new RecordField(new JavaParameterName("name"), "name",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("String")), true, false, false),
                new RecordField(new JavaParameterName("age"), "age",
                        TypeDescriptor.qualified("java.lang", new JavaTypeName.Reserved("Integer")), false, false, false)
        ), false);

        String source = relaxedWriter.toJavaFile(model).toString();

        assertFalse(source.contains("Objects.requireNonNull"));
    }
}
