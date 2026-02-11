package nl.stijlaartit.generator.engine.model;

import nl.stijlaartit.generator.engine.domain.EnumModel;
import nl.stijlaartit.generator.engine.domain.EnumValueType;
import nl.stijlaartit.generator.engine.domain.FieldModel;
import nl.stijlaartit.generator.engine.domain.OneOfModel;
import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.RecordModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelWriterTest {

    private final ModelWriter writer = new ModelWriter("com.example.models");

    @Test
    void generatesRecordWithSingleField() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("package com.example.models;"));
        assertTrue(source.contains("record User("));
        assertTrue(source.contains("String name"));
    }

    @Test
    void generatesRecordWithMultipleFields() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true),
                new FieldModel("age", "age",
                        TypeDescriptor.simple("java.lang.Integer"), false)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("String name"));
        assertTrue(source.contains("@Nullable Integer age"));
        assertTrue(source.contains("import org.jspecify.annotations.Nullable;"));
    }

    @Test
    void addsJsonPropertyWhenNamesDisagree() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("firstName", "first_name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(\"first_name\")"));
        assertTrue(source.contains("import com.fasterxml.jackson.annotation.JsonProperty;"));
    }

    @Test
    void omitsJsonPropertyWhenNamesMatch() {
        RecordModel model = new RecordModel("User", List.of(
                new FieldModel("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertFalse(source.contains("@JsonProperty"));
        assertFalse(source.contains("import com.fasterxml.jackson.annotation"));
    }

    @Test
    void resolvesComplexTypeFromModelsPackage() {
        RecordModel model = new RecordModel("Owner", List.of(
                new FieldModel("pet", "pet",
                        TypeDescriptor.complex("Pet"), true)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Pet pet"));
    }

    @Test
    void resolvesListType() {
        RecordModel model = new RecordModel("Pet", List.of(
                new FieldModel("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), false)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<String> tags"));
    }

    @Test
    void resolvesMapType() {
        RecordModel model = new RecordModel("Config", List.of(
                new FieldModel("metadata", "metadata",
                        TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")), false)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Map<String, Integer> metadata"));
    }

    @Test
    void resolvesListOfComplexType() {
        RecordModel model = new RecordModel("Pet", List.of(
                new FieldModel("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.complex("Tag")), false)
        ), List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<Tag> tags"));
    }

    @Test
    void generatesEnumWithJsonPropertyValues() {
        EnumModel model = new EnumModel("PetStatus", List.of(
                "available",
                "pending",
                "sold"
        ), EnumValueType.STRING, List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("enum PetStatus"));
        assertTrue(source.contains("@JsonProperty(\"available\")"));
        assertTrue(source.contains("@JsonProperty(\"pending\")"));
        assertTrue(source.contains("@JsonProperty(\"sold\")"));
    }

    @Test
    void generatesNumericEnumWithLiteralJsonProperty() {
        EnumModel model = new EnumModel("Mode", List.of(
                "-1",
                "0",
                "1"
        ), EnumValueType.NUMBER, List.of());

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonValue"));
        assertTrue(source.contains("new BigDecimal(\"-1\")"));
        assertTrue(source.contains("new BigDecimal(\"0\")"));
        assertTrue(source.contains("new BigDecimal(\"1\")"));
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
                        true
                )),
                List.of()
        );

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("record SessionResponse"));
        assertTrue(source.contains("@JsonValue"));
        assertTrue(source.contains("Map<String, Object> value"));
    }

    @Test
    void generatesOneOfInterfaceWithDiscriminator() {
        OneOfModel model = new OneOfModel(
                "QueueObjectCurrentlyPlaying",
                List.of(
                        new OneOfVariant("TrackObject", "track"),
                        new OneOfVariant("EpisodeObject", "episode")
                ),
                "type"
        );

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonTypeInfo("));
        assertTrue(source.contains("property = \"type\""));
        assertTrue(source.contains("@JsonSubTypes("));
        assertTrue(source.contains("JsonSubTypes.Type(value = TrackObject.class, name = \"track\")"));
        assertTrue(source.contains("JsonSubTypes.Type(value = EpisodeObject.class, name = \"episode\")"));
    }
}
