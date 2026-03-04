package nl.stijlaartit.variants.jackson2.generated;

import nl.stijlaartit.variants.jackson2.generated.client.DefaultApi;
import nl.stijlaartit.variants.jackson2.generated.models.GetInlineResponseBody200ResponseItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class InlineResponseBodyTest {

    @Mock
    private DefaultApi defaultApi;

    @Test
    void shouldHaveGeneratedResponseBody() {
        // Validates the generated types
        List<GetInlineResponseBody200ResponseItem> result =  defaultApi.getInlineResponseBody();
        assertThat(result).isNotNull();
    }
}
