package nl.stijlaartit.variants.jackson2.generated;

import nl.stijlaartit.variants.jackson2.generated.models.AdditionalPropertiesEmptyObject;
import nl.stijlaartit.variants.jackson2.generated.models.AdditionalPropertiesWithOtherProperties;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AdditionalPropertiesTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeAdditionalPropertiesBesidesDefinedProperties() throws Exception {
        final var original = new AdditionalPropertiesWithOtherProperties("name", Map.of("foo", "bar"));
        String serialized = objectMapper.writeValueAsString(original);
        assertThat(serialized).isEqualTo("{\"name\":\"name\",\"foo\":\"bar\"}");
    }


    @Test
    void shouldSymmetricallySerializeAdditionalPropertiesWithOtherProperties() throws Exception {
        AdditionalPropertiesWithOtherProperties original = new AdditionalPropertiesWithOtherProperties("name", Map.of("foo", "bar"));
        assertSerializesSymmetrical(
                original,
                AdditionalPropertiesWithOtherProperties.class
        );
    }

    @Test
    void shouldSymmetricallySerializeAdditionalPropertiesEmptyObject() throws Exception {
        assertSerializesSymmetrical(new AdditionalPropertiesEmptyObject(Map.of()), AdditionalPropertiesEmptyObject.class);
        assertSerializesSymmetrical(new AdditionalPropertiesEmptyObject(Map.of("foo", "bar")), AdditionalPropertiesEmptyObject.class);
    }


    private <T> void assertSerializesSymmetrical(T original, Class<T> type) throws Exception {
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, type);
        assertThat(deserialized).isEqualTo(original);
    }
}
