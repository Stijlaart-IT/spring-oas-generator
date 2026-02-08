package nl.stijlaartit.generator.model;

import com.palantir.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelWriterTest {

    private final ModelWriter writer = new ModelWriter("com.example.models");

    @Test
    void generatesRecordWithSingleField() {
        ModelDescriptor model = new ModelDescriptor("User", List.of(
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
        ModelDescriptor model = new ModelDescriptor("User", List.of(
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
        ModelDescriptor model = new ModelDescriptor("User", List.of(
                new FieldDescriptor("firstName", "first_name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("@JsonProperty(\"first_name\")"));
        assertTrue(source.contains("import com.fasterxml.jackson.annotation.JsonProperty;"));
    }

    @Test
    void omitsJsonPropertyWhenNamesMatch() {
        ModelDescriptor model = new ModelDescriptor("User", List.of(
                new FieldDescriptor("name", "name",
                        TypeDescriptor.simple("java.lang.String"), true)
        ));

        String source = writer.toJavaFile(model).toString();

        assertFalse(source.contains("@JsonProperty"));
        assertFalse(source.contains("import com.fasterxml.jackson.annotation"));
    }

    @Test
    void resolvesComplexTypeFromModelsPackage() {
        ModelDescriptor model = new ModelDescriptor("Owner", List.of(
                new FieldDescriptor("pet", "pet",
                        TypeDescriptor.complex("Pet"), true)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Pet pet"));
    }

    @Test
    void resolvesListType() {
        ModelDescriptor model = new ModelDescriptor("Pet", List.of(
                new FieldDescriptor("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.simple("java.lang.String")), false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<String> tags"));
    }

    @Test
    void resolvesMapType() {
        ModelDescriptor model = new ModelDescriptor("Config", List.of(
                new FieldDescriptor("metadata", "metadata",
                        TypeDescriptor.map(TypeDescriptor.simple("java.lang.Integer")), false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("Map<String, Integer> metadata"));
    }

    @Test
    void resolvesListOfComplexType() {
        ModelDescriptor model = new ModelDescriptor("Pet", List.of(
                new FieldDescriptor("tags", "tags",
                        TypeDescriptor.list(TypeDescriptor.complex("Tag")), false)
        ));

        String source = writer.toJavaFile(model).toString();

        assertTrue(source.contains("List<Tag> tags"));
    }
}
