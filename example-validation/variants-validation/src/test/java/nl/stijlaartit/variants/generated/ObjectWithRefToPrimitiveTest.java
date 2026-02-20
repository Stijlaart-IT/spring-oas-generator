package nl.stijlaartit.variants.generated;

import nl.stijlaartit.variants.generated.models.ObjectWithRefToPrimitive;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectWithRefToPrimitiveTest {

    @Test
    void shouldProperlyTypeRefs() {
        final var objectWithRefToPrimitive = new ObjectWithRefToPrimitive(42);
        // Should generate key as Integer
        Integer key = objectWithRefToPrimitive.key();
        assertThat(key).isEqualTo(42);
    }
}
