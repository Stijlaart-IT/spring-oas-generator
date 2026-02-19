package nl.stijlaartit.variants.generated;

import nl.stijlaartit.variants.generated.client.DefaultApi;
import nl.stijlaartit.variants.generated.models.PutInlineRequestBodyRequestItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class InlineRequestBodyTest {

    @Mock
    private DefaultApi defaultApi;

    @Test
    void shouldHaveGeneratedRequestBody() {
        // Validates the generated types
        List<PutInlineRequestBodyRequestItem> payload = List.of(
                PutInlineRequestBodyRequestItem.builder().itemName("name").build()
        );
        defaultApi.putInlineRequestBody(payload);
    }
}
