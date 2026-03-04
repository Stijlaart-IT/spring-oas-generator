package nl.stijlaartit.variants.jackson2.generated;

import nl.stijlaartit.variants.jackson2.generated.models.NullWrapper;
import nl.stijlaartit.variants.jackson2.generated.models.RequiredAndNullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequiredAndNullableTest {

    @Test
    void builderAllowsNullForNullableFields() {
        var model = RequiredAndNullable.builder()
                .requiredNonNullable("required")
                .optionalNonNullable(null)
                .requiredNullable(null)
                .optionalNullable((String) null)
                .build();

        assertThat(model.requiredNonNullable()).isEqualTo("required");
        assertThat(model.optionalNonNullable()).isNull();
        assertThat(model.requiredNullable()).isNull();
        assertThat(model.optionalNullable()).isEqualTo(new NullWrapper<>(null));
    }

    @Test
    void builderRejectsNullForRequiredNonNullableField() {
        assertThrows(NullPointerException.class, () -> RequiredAndNullable.builder().requiredNonNullable(null)
                .optionalNonNullable("value")
                .requiredNullable("value")
                .optionalNullable(new NullWrapper<>("value"))
                .build());
    }
}
