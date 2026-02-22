package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.domain.file.JavaTypeName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaTypeNameTest {

    @Test
    void validateNameRejectsInvalidIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new JavaTypeName.Generated("_Pet"));
        assertThrows(IllegalArgumentException.class, () -> new JavaTypeName.Generated("pet"));
        assertThrows(IllegalArgumentException.class, () -> new JavaTypeName.Generated("Object"));
        assertThrows(IllegalArgumentException.class, () -> new JavaTypeName.Generated("Pet-Name"));
    }

    @Test
    void validateNameAcceptsValidIdentifiers() {
        assertEquals("Pet", new JavaTypeName.Generated("Pet").value());
    }

}
