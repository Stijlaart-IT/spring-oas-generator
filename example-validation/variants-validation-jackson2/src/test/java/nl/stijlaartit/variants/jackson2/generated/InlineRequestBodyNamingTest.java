package nl.stijlaartit.variants.jackson2.generated;

import nl.stijlaartit.variants.jackson2.generated.models.PutInlineRequestBodyRequestItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InlineRequestBodyNamingTest {

    @Test
    void inlineRequestBodyUsesPathAndMethodFallbackName() {
        assertThat(PutInlineRequestBodyRequestItem.class.getSimpleName())
                .isEqualTo("PutInlineRequestBodyRequestItem");
    }
}
