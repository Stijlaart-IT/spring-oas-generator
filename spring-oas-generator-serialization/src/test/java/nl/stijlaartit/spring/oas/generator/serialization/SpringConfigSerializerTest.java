package nl.stijlaartit.spring.oas.generator.serialization;

import nl.stijlaartit.spring.oas.generator.domain.file.SpringConfigFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringConfigSerializerTest {

    private final SpringConfigSerializer serializer = new SpringConfigSerializer();

    @Test
    void generatesApiConfigurationClassWithImportHttpServicesAnnotation() {
        SpringConfigFile file = new SpringConfigFile(
                "com.example.generated.config",
                "com.example.generated.client",
                "petstore",
                java.util.List.of("PetApi", "StoreApi", "UserApi")
        );

        String source = serializer.toJavaFile(file).toString();

        assertTrue(source.contains("package com.example.generated.config;"));
        assertTrue(source.contains("class ApiConfiguration"));
        assertTrue(source.contains("@Configuration"));
        assertTrue(source.contains("@ImportHttpServices("));
        assertTrue(source.contains("group = \"petstore\""));
        assertTrue(source.contains("types = {PetApi.class, StoreApi.class, UserApi.class}"));
        assertTrue(source.contains("import com.example.generated.client.PetApi;"));
        assertTrue(source.contains("import com.example.generated.client.StoreApi;"));
        assertTrue(source.contains("import com.example.generated.client.UserApi;"));
    }
}
