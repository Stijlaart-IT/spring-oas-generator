package nl.stijlaartit.variants.generated;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import nl.stijlaartit.variants.generated.client.DefaultApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestHeaderIntegrationTest {

    private static MockWebServer server;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void afterAll() {
        server.close();
    }

    @Test
    void shouldSendRequestHeader() throws InterruptedException {
        server.enqueue(new MockResponse.Builder().code(204).build());

        RestClient restClient = RestClient.create("http://" + server.getHostName() + ":" + server.getPort());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        DefaultApi defaultApi = factory.createClient(DefaultApi.class);

        String requestId = UUID.randomUUID().toString();
        defaultApi.getPing(requestId);

        var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getUrl().encodedPath()).isEqualTo("/ping");
        assertThat(request.getHeaders().get("X-Request-ID")).isEqualTo(requestId);
    }
}
