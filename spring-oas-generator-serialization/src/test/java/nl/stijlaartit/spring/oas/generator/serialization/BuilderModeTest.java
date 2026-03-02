package nl.stijlaartit.spring.oas.generator.serialization;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuilderModeTest {

    @Test
    void parse_validMode_returnsMode() {
        assertEquals(Optional.of(BuilderMode.DISABLED), BuilderMode.parse("DISABLED"));
        assertEquals(Optional.of(BuilderMode.STRICT), BuilderMode.parse("strict"));
        assertEquals(Optional.of(BuilderMode.RELAXED), BuilderMode.parse(" relaxed "));
    }

    @Test
    void parse_invalidMode_returnsEmpty() {
        assertEquals(Optional.empty(), BuilderMode.parse(null));
        assertEquals(Optional.empty(), BuilderMode.parse(" "));
        assertEquals(Optional.empty(), BuilderMode.parse("other"));
    }
}
