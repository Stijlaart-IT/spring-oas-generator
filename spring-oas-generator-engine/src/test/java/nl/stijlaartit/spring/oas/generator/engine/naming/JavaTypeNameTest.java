package nl.stijlaartit.spring.oas.generator.engine.naming;

import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaInstance;
import nl.stijlaartit.spring.oas.generator.engine.schemas.SchemaParent;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaTypeNameTest {

    @Test
    void validateNameRejectsInvalidIdentifiers() {
        assertThrows(IllegalStateException.class, () -> new JavaTypeName.Generated("_Pet"));
        assertThrows(IllegalStateException.class, () -> new JavaTypeName.Generated("pet"));
        assertThrows(IllegalStateException.class, () -> new JavaTypeName.Generated("Object"));
        assertThrows(IllegalStateException.class, () -> new JavaTypeName.Generated("Pet-Name"));
    }

    @Test
    void validateNameAcceptsValidIdentifiers() {
        assertEquals("Pet", new JavaTypeName.Generated("Pet").value());
    }

}
