package nl.stijlaartit.spring.oas.generator.engine.model;

import nl.stijlaartit.spring.oas.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.engine.domain.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.engine.GeneratedAnnotation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnionModelSerializerTest {

    private final UnionModelSerializer writer = new UnionModelSerializer("com.example.models");

    @Test
    void generatesOneOfInterfaceWithDiscriminator() {
        UnionModelFile model = new UnionModelFile(
                "QueueObjectCurrentlyPlaying",
                List.of(
                        new OneOfVariant("TrackObject", "track"),
                        new OneOfVariant("EpisodeObject", "episode")
                ),
                "type"
        );

        String source = writer.toJavaFile(model).toString();

        assertGeneratedAnnotation(source);
        assertTrue(source.contains("@JsonTypeInfo("));
        assertTrue(source.contains("property = \"type\""));
        assertTrue(source.contains("@JsonSubTypes("));
        assertTrue(source.contains("JsonSubTypes.Type(value = TrackObject.class, name = \"track\")"));
        assertTrue(source.contains("JsonSubTypes.Type(value = EpisodeObject.class, name = \"episode\")"));
    }

    private static void assertGeneratedAnnotation(String source) {
        assertTrue(source.contains("value = \"" + GeneratedAnnotation.VALUE + "\""));
        Pattern pattern = Pattern.compile(
                "@(?:javax\\.annotation\\.processing\\.)?Generated\\(\\s*value = \".+?\"\\s*,\\s*date = \"\\d{4}-\\d{2}-\\d{2}T[^\"]+\"\\s*\\)",
                Pattern.DOTALL
        );
        assertTrue(pattern.matcher(source).find());
    }
}
