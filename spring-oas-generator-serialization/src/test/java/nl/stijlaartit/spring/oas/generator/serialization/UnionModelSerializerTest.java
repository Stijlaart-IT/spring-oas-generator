package nl.stijlaartit.spring.oas.generator.serialization;

import nl.stijlaartit.spring.oas.generator.domain.file.OneOfVariant;
import nl.stijlaartit.spring.oas.generator.domain.file.UnionModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnionModelSerializerTest {

    private final UnionModelSerializer writer = new UnionModelSerializer("com.example.models");

    @Test
    void generatesOneOfInterfaceWithDiscriminator() {
        UnionModelFile model = new UnionModelFile(
                new JavaTypeName.Generated("QueueObjectCurrentlyPlaying"),
                List.of(
                        new OneOfVariant(new JavaTypeName.Generated("TrackObject"), "track"),
                        new OneOfVariant(new JavaTypeName.Generated("EpisodeObject"), "episode")
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
