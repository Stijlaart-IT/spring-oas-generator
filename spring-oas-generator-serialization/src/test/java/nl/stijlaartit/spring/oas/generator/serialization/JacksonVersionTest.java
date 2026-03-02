package nl.stijlaartit.spring.oas.generator.serialization;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonVersionTest {

    @Test
    void parse_validVersion_returnsVersion() {
        assertEquals(Optional.of(JacksonVersion.V2), JacksonVersion.parse("2"));
        assertEquals(Optional.of(JacksonVersion.V3), JacksonVersion.parse("3"));
    }

    @Test
    void parse_invalidVersion_returnsEmpty() {
        assertEquals(Optional.empty(), JacksonVersion.parse(null));
        assertEquals(Optional.empty(), JacksonVersion.parse(" "));
        assertEquals(Optional.empty(), JacksonVersion.parse("4"));
    }
}
