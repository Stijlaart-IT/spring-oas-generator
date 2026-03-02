package nl.stijlaartit.spring.oas.generator.serialization;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NullWrapperSerializerTest {

    private final JacksonV3NullWrapperSerializer writer = new JacksonV3NullWrapperSerializer("com.example.models");

    @Test
    void addsGeneratedAnnotationToNullWrapperRecord() {
        String source = writer.toJavaFile().toString();

        assertTrue(source.contains("value = \"" + GeneratedAnnotation.VALUE + "\""));
        Pattern pattern = Pattern.compile(
                "@(?:javax\\.annotation\\.processing\\.)?Generated\\(\\s*value = \".+?\"\\s*,\\s*date = \"\\d{4}-\\d{2}-\\d{2}T[^\"]+\"\\s*\\)",
                Pattern.DOTALL
        );
        assertTrue(pattern.matcher(source).find());
    }

    @Test
    void defaultConfig_usesJackson3Types() {
        String source = writer.toJavaFile().toString();

        assertTrue(source.contains("import tools.jackson.databind.ValueDeserializer;"));
        assertTrue(source.contains("import tools.jackson.databind.annotation.JsonDeserialize;"));
    }

    @Test
    void jackson2Serializer_usesJackson2Types() {
        JacksonV2NullWrapperSerializer jackson2Writer = new JacksonV2NullWrapperSerializer("com.example.models");

        String source = jackson2Writer.toJavaFile().toString();

        assertTrue(source.contains("import com.fasterxml.jackson.databind.JsonDeserializer;"));
        assertTrue(source.contains("import com.fasterxml.jackson.databind.annotation.JsonDeserialize;"));
        assertTrue(source.contains("import com.fasterxml.jackson.databind.deser.ContextualDeserializer;"));
    }
}
