package nl.stijlaartit.generator.engine.model;

import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.UnionModelFile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UnionModelWriterTest {

    private final UnionModelWriter writer = new UnionModelWriter("com.example.models");

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

        assertTrue(source.contains("@JsonTypeInfo("));
        assertTrue(source.contains("property = \"type\""));
        assertTrue(source.contains("@JsonSubTypes("));
        assertTrue(source.contains("JsonSubTypes.Type(value = TrackObject.class, name = \"track\")"));
        assertTrue(source.contains("JsonSubTypes.Type(value = EpisodeObject.class, name = \"episode\")"));
    }
}
