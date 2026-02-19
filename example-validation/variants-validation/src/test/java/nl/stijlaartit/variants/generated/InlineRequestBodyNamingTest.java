package nl.stijlaartit.variants.generated;

import nl.stijlaartit.variants.generated.models.PutInlineRequestBodyRequestItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InlineRequestBodyNamingTest {

    @Test
    void inlineRequestBodyUsesPathAndMethodFallbackName() {
        assertThat(PutInlineRequestBodyRequestItem.class.getSimpleName())
                .isEqualTo("PutInlineRequestBodyRequestItem");
    }
}
