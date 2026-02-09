package nl.stijlaartit.generation.model;

import com.palantir.javapoet.JavaFile;
import nl.stijlaartit.generator.model.TypeDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelWriterTest {

    private final ModelWriter writer = new ModelWriter("com.example.models");

    @Test
    void generatesRecordWithSingleField() {
        ModelDescriptor model = ModelDescriptor.record("User", List.of(
                new FieldDescriptor("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("package com.example.models;"));
        assertTrue(source.contains("record User("));
        assertTrue(source.contains("String name"));
    }

    @Test
    void generatesRecordWithMultipleFields() {
        ModelDescriptor model = ModelDescriptor.record("User", List.of(
                new FieldDescriptor("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true),
                new FieldDescriptor("age", "age",
                        TypeDescriptor.simple("java.lang.Integer"), false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("String name"));
        assertTrue(source.contains("Integer age"));
    }

    @Test
    void addsJsonPropertyWhenNamesDisagree() {
        ModelDescriptor model = ModelDescriptor.record("User", List.of(
                new FieldDescriptor("firstName", "first_name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(\"first_name\")"));
        assertTrue(source.contains("import com.fasterxml.jackson.annotation.JsonProperty;"));
    }

    @Test
    void omitsJsonPropertyWhenNamesMatch() {
        ModelDescriptor model = ModelDescriptor.record("User", List.of(
                new FieldDescriptor("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ));

        String source = writer.toJavaFile(model).toString();

        assertFalse(source.contains("@JsonProperty"));
        assertFalse(source.contains("import com.fasterxml.jackson.annotation"));
    }

    @Test
    void resolvesComplexTypeFromModelsPackage() {
        ModelDescriptor model = ModelDescriptor.record("Owner", List.of(
                new FieldDescriptor("pet", "pet",
                        TypeDescriptor.complex("Pet"), true)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Pet pet"));
    }

    @Test
    void resolvesListType() {
        ModelDescriptor model = ModelDescriptor.record("Pet", List.of(
                new FieldDescriptor("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<String> tags"));
    }

    @Test
    void resolvesMapType() {
        ModelDescriptor model = ModelDescriptor.record("Config", List.of(
                new FieldDescriptor("metadata", "metadata",
                        TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")), false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Map<String, Integer> metadata"));
    }

    @Test
    void resolvesListOfComplexType() {
        ModelDescriptor model = ModelDescriptor.record("Pet", List.of(
                new FieldDescriptor("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.complex("Tag")), false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<Tag> tags"));
    }

    @Test
    void generatesEnumWithJsonPropertyValues() {
        ModelDescriptor model = ModelDescriptor.enumModel("PetStatus", List.of(
                "available",
                "pending",
                "sold"
        ), EnumValueType.STRING);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("enum PetStatus"));
        assertTrue(source.contains("@JsonProperty(\"available\")"));
        assertTrue(source.contains("@JsonProperty(\"pending\")"));
        assertTrue(source.contains("@JsonProperty(\"sold\")"));
    }

    @Test
    void generatesNumericEnumWithLiteralJsonProperty() {
        ModelDescriptor model = ModelDescriptor.enumModel("Mode", List.of(
                "-1",
                "0",
                "1"
        ), EnumValueType.NUMBER);

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonValue"));
        assertTrue(source.contains("new BigDecimal(\"-1\")"));
        assertTrue(source.contains("new BigDecimal(\"0\")"));
        assertTrue(source.contains("new BigDecimal(\"1\")"));
    }
}
