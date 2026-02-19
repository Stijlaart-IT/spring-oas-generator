package nl.stijlaartit.variants.generated;

import nl.stijlaartit.variants.generated.models.AdditionalPropertiesEmptyObject;
import nl.stijlaartit.variants.generated.models.AdditionalPropertiesTrue;
import nl.stijlaartit.variants.generated.models.AdditionalPropertiesWithOtherProperties;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AdditionalPropertiesTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeAdditionalPropertiesBesidesDefinedProperties() {
        final var original = new AdditionalPropertiesWithOtherProperties("name", Map.of("foo", "bar"));
        String serialized = objectMapper.writeValueAsString(original);
        assertThat(serialized).isEqualTo("{\"name\":\"name\",\"foo\":\"bar\"}");
    }


    @Test
    void shouldSymmetricallySerializeAdditionalPropertiesWithOtherProperties() {
        AdditionalPropertiesWithOtherProperties original = new AdditionalPropertiesWithOtherProperties("name", Map.of("foo", "bar"));
        assertSerializesSymmetrical(
                original,
                AdditionalPropertiesWithOtherProperties.class
        );
    }

    @Test
    void shouldSymmetricallySerializeAdditionalPropertiesTrue() {
        assertSerializesSymmetrical(new AdditionalPropertiesTrue(Map.of()), AdditionalPropertiesTrue.class);
        assertSerializesSymmetrical(new AdditionalPropertiesTrue(Map.of("foo", "bar")), AdditionalPropertiesTrue.class);
    }

    @Test
    void shouldSymmetricallySerializeAdditionalPropertiesEmptyObject() {
        assertSerializesSymmetrical(new AdditionalPropertiesEmptyObject(Map.of()), AdditionalPropertiesEmptyObject.class);
        assertSerializesSymmetrical(new AdditionalPropertiesEmptyObject(Map.of("foo", "bar")), AdditionalPropertiesEmptyObject.class);
    }


    private <T> void assertSerializesSymmetrical(T original, Class<T> type) {
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, type);
        assertThat(deserialized).isEqualTo(original);
    }
}
