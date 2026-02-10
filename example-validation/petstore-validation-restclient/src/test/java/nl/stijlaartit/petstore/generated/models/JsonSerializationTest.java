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

    private <T> void assertSerializesSymmetrical(T original, Class<T> type) throws Exception {
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, type);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void apiResponse() throws Exception {
        var original = new ApiResponse(200, "unknown", "An error occurred");
        assertSerializesSymmetrical(original, ApiResponse.class);
    }

    @Test
    void category() throws Exception {
        var original = new Category(1L, "Dogs");
        assertSerializesSymmetrical(original, Category.class);
    }

    @Test
    void order() throws Exception {
        var original = new Order(10L, 198772L, 7,
                OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC),
                OrderStatus.APPROVED, true);
        assertSerializesSymmetrical(original, Order.class);
    }

    @Test
    void pet() throws Exception {
        var category = new Category(1L, "Dogs");
        var tags = List.of(new Tag(0L, "friendly"), new Tag(1L, "large"));
        var original = new Pet(10L, "Doggo", category,
                List.of("http://example.com/photo1.jpg"), tags, PetStatus.AVAILABLE);
        assertSerializesSymmetrical(original, Pet.class);
    }

    @Test
    void tag() throws Exception {
        var original = new Tag(0L, "friendly");
        assertSerializesSymmetrical(original, Tag.class);
    }

    @Test
    void user() throws Exception {
        var original = new User(10L, "theUser", "John", "James",
                "john@email.com", "12345", "12345", 1);
        assertSerializesSymmetrical(original, User.class);
    }

    @Test
    void orderStatus() throws Exception {
        var original = OrderStatus.values()[0];
        assertSerializesSymmetrical(original, OrderStatus.class);
    }

    @Test
    void petStatus() throws Exception {
        var original = PetStatus.values()[0];
        assertSerializesSymmetrical(original, PetStatus.class);
    }
}
