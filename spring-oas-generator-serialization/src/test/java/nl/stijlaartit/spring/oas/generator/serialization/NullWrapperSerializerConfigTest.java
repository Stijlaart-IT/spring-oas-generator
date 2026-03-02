package nl.stijlaartit.spring.oas.generator.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NullWrapperSerializerConfigTest {

    @Test
    void defaultConfig_usesJackson3() {
        NullWrapperSerializerConfig config = NullWrapperSerializerConfig.defaultConfig();

        assertEquals(JacksonVersion.V3, config.jacksonVersion());
    }
}
