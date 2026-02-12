package nl.stijlaartit.inlinerequestbody.generated.models;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSerializationTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private <T> void assertSerializesSymmetrical(T original, Class<T> type) {
        var json = objectMapper.writeValueAsString(original);
        var deserialized = objectMapper.readValue(json, type);
        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void operationRequestItem() {
        var original = new PutV1RecordsRecordIdItemsRequestItem(
                "item-name",
                PutV1RecordsRecordIdItemsRequestItemItemType.TYPEA,
                "application/pdf",
                "item-123",
                new BigDecimal("2"),
                "group-456",
                "related-789"
        );
        assertSerializesSymmetrical(original, PutV1RecordsRecordIdItemsRequestItem.class);
    }

    @Test
    void operationResponseItem() {
        var original = new GetV1RecordsRecordIdMissingItemsResponseItem(
                PutV1RecordsRecordIdItemsRequestItemItemType.TYPEB,
                new BigDecimal("3"),
                List.of("pdf", "tiff")
        );
        assertSerializesSymmetrical(original, GetV1RecordsRecordIdMissingItemsResponseItem.class);
    }

    @Test
    void operationRequestItemItemType() {
        var original = PutV1RecordsRecordIdItemsRequestItemItemType.TYPEC;
        assertSerializesSymmetrical(original, PutV1RecordsRecordIdItemsRequestItemItemType.class);
    }

    @Test
    void operationResponseItemItemType() {
        var original = PutV1RecordsRecordIdItemsRequestItemItemType.TYPED;
        assertSerializesSymmetrical(original, PutV1RecordsRecordIdItemsRequestItemItemType.class);
    }
}
