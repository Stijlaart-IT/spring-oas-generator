package nl.stijlaartit.variants.generated;

import nl.stijlaartit.variants.generated.models.AllOfFirstObject;
import nl.stijlaartit.variants.generated.models.AllOfSecondObject;
import nl.stijlaartit.variants.generated.models.AllOfTwoObjects;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllOfTwoObjectsTest {

    @Test
    public void shouldCreateDistinctObjects() {
        AllOfTwoObjects combined = new AllOfTwoObjects("name", 16);
        AllOfFirstObject nameObject = new AllOfFirstObject("name");
        AllOfSecondObject ageObject = new AllOfSecondObject(16);

        assertThat(combined).isNotNull();
        assertThat(nameObject).isNotNull();
        assertThat(ageObject).isNotNull();

        assertThat(combined).isNotInstanceOf(AllOfFirstObject.class);
        assertThat(combined).isNotInstanceOf(AllOfSecondObject.class);
    }

}
