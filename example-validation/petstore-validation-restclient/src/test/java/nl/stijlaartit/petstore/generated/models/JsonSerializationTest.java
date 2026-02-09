package nl.stijlaartit.petstore.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void apiResponse() throws Exception {
        var original = new ApiResponse(200, "unknown", "An error occurred");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, ApiResponse.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void category() throws Exception {
        var original = new Category(1L, "Dogs");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, Category.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void order() throws Exception {
        var original = new Order(10L, 198772L, 7,
                OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC),
                "approved", true);
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, Order.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void pet() throws Exception {
        var category = new Category(1L, "Dogs");
        var tags = List.of(new Tag(0L, "friendly"), new Tag(1L, "large"));
        var original = new Pet(10L, "Doggo", category,
                List.of("http://example.com/photo1.jpg"), tags, "available");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, Pet.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void tag() throws Exception {
        var original = new Tag(0L, "friendly");
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, Tag.class);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void user() throws Exception {
        var original = new User(10L, "theUser", "John", "James",
                "john@email.com", "12345", "12345", 1);
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, User.class);
        assertThat(deserialized).isEqualTo(original);
    }
}
