package nl.stijlaartit.spring.oas.generator.serialization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RecordModelWriterConfigTest {

    @Test
    void defaultConfig_disablesJacksonRequiredOverrideByDefault() {
        RecordModelWriterConfig config = RecordModelWriterConfig.defaultConfig();

        assertFalse(config.disableJacksonRequired());
    }
}
