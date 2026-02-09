package nl.stijlaartit.spotify.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void imageObject() throws Exception {
        var original = new ImageObject("https://example.com/image.jpg", 640, 640);
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, ImageObject.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void albumRestrictionObject() throws Exception {
        var original = new AlbumRestrictionObject(AlbumRestrictionObjectReason.EXPLICIT);
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, AlbumRestrictionObject.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void artistObject() throws Exception {
        var images = List.of(new ImageObject("https://example.com/artist.jpg", 300, 300));
        var original = new ArtistObject(null, null, List.of("pop"),
                "https://api.spotify.com/v1/artists/1", "1", images,
                "Test Artist", 42, ArtistObjectType.ARTIST, "spotify:artist:1");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, ArtistObject.class);
        assertThat(deserialized).isEqualTo(original);
    }
}
