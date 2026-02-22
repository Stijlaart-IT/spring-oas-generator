package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaParameterName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaParameterNameTest {

    @Test
    void rejectsInvalidIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new JavaParameterName(null));
        assertThrows(IllegalArgumentException.class, () -> new JavaParameterName("1invalid"));
        assertThrows(IllegalArgumentException.class, () -> new JavaParameterName("not-valid"));
        assertThrows(IllegalArgumentException.class, () -> new JavaParameterName("class"));
        assertThrows(IllegalArgumentException.class, () -> new JavaParameterName(""));
    }

    @Test
    void acceptsValidIdentifiers() {
        assertEquals("petId", new JavaParameterName("petId").value());
    }
}
