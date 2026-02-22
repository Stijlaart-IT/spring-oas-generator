package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaMethodName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaMethodNameTest {

    @Test
    void rejectsInvalidIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new JavaMethodName(null));
        assertThrows(IllegalArgumentException.class, () -> new JavaMethodName("1invalid"));
        assertThrows(IllegalArgumentException.class, () -> new JavaMethodName("not-valid"));
        assertThrows(IllegalArgumentException.class, () -> new JavaMethodName("class"));
        assertThrows(IllegalArgumentException.class, () -> new JavaMethodName(""));
    }

    @Test
    void acceptsValidIdentifiers() {
        assertEquals("getPetById", new JavaMethodName("getPetById").value());
    }
}
