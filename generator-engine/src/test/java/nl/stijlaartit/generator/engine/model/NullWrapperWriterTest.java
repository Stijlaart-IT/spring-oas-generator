package nl.stijlaartit.generator.engine.model;

import nl.stijlaartit.generator.engine.GeneratedAnnotation;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NullWrapperWriterTest {

    private final NullWrapperWriter writer = new NullWrapperWriter("com.example.models");

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
}
