package nl.stijlaartit.variants.generated;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import nl.stijlaartit.variants.generated.models.NullWrapper;
import nl.stijlaartit.variants.generated.models.RequiredAndNullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RequiredAndNullableIntegrationTest {

    private static MockWebServer server;

    @Autowired
    private RestClient restClient;


    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = new MockWebServer();
        server.start();
        System.setProperty("mockserver.url", "http://" + server.getHostName() + ":" + server.getPort());
    }

    @AfterAll
    static void afterAll() {
        server.close();
    }

    @Test
    void shouldReadRequiredAndNullableFromString() {
        String input1 = "{\"requiredNullable\":null,\"requiredNonNullable\":\"value\"}";
        String input2 = "{\"requiredNullable\":null,\"requiredNonNullable\":\"value\",\"optionalNullable\":null}";
        String input3 = "{\"requiredNullable\":\"value\",\"requiredNonNullable\":\"value\",\"optionalNullable\":\"value\",\"optionalNonNullable\":\"value\"}";

        final var result1 = objectMapper.readValue(input1, RequiredAndNullable.class);
        final var result2 = objectMapper.readValue(input2, RequiredAndNullable.class);
        final var result3 = objectMapper.readValue(input3, RequiredAndNullable.class);

        RequiredAndNullable expected1 = new RequiredAndNullable(null, "value", null, null);
        RequiredAndNullable expected2 = new RequiredAndNullable(null, "value", new NullWrapper<>(null), null);
        RequiredAndNullable expected3 = new RequiredAndNullable("value", "value", new NullWrapper<>("value"), "value");

        assertThat(result1).isEqualTo(expected1);
        assertThat(result2).isEqualTo(expected2);
        assertThat(result3).isEqualTo(expected3);
    }

    @Test
    void postRequiredAndNullable() throws InterruptedException {

        String expectedBody1Serialized = "{\"requiredNullable\":null,\"requiredNonNullable\":\"required\"}";
        String expectedBody2Serialized = "{\"requiredNullable\":null,\"requiredNonNullable\":\"required\",\"optionalNullable\":null}";


        server.enqueue(new MockResponse.Builder().addHeader("Content-Type", "application/json").body(expectedBody1Serialized).build());
        server.enqueue(new MockResponse.Builder().addHeader("Content-Type", "application/json").body(expectedBody2Serialized).build());

        RequiredAndNullable body1 = new RequiredAndNullable(null, "required", null, null);
        RequiredAndNullable body2 = new RequiredAndNullable(null, "required", new NullWrapper<>(null), null);

        RequiredAndNullable response1 = restClient.post()
                .uri("/required-and-nullable")
                .body(body1)
                .retrieve()
                .body(RequiredAndNullable.class);

        RequiredAndNullable response2 = restClient.post()
                .uri("/required-and-nullable")
                .body(body2)
                .retrieve()
                .body(RequiredAndNullable.class);

        var request1 = server.takeRequest();
        var request2 = server.takeRequest();
        assertThat(request1.getBody().utf8()).isEqualTo(expectedBody1Serialized);
        assertThat(request2.getBody().utf8()).isEqualTo(expectedBody2Serialized);


        assertThat(response1).isEqualTo(body1);
        assertThat(response2).isEqualTo(body2);
    }
}
