package nl.stijlaartit.springconfig.generated;

import mockwebserver3.MockWebServer;
import mockwebserver3.MockResponse;
import nl.stijlaartit.springconfig.generated.client.PetApi;
import nl.stijlaartit.springconfig.generated.models.Category;
import nl.stijlaartit.springconfig.generated.models.Pet;
import nl.stijlaartit.springconfig.generated.models.PetStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("config-integration")
public class ConfigIntegrationTest {

    private static final MockWebServer server;

    static {
        try {
            server = new MockWebServer();
            server.start();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Autowired
    private PetApi petApi;

    @DynamicPropertySource
    static void setMockServerEnvVariable(DynamicPropertyRegistry registry) {
        registry.add("MOCKSERVER_URL", () -> "http://" + server.getHostName() + ":" + server.getPort());
    }

    @AfterAll
    static void afterAll() {
        server.close();
    }

    @Test
    void shouldReachMockServerWhenCallingPetApi() throws InterruptedException {
        server.enqueue(new MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("{\"id\":10,\"name\":\"Doggo\",\"category\":{\"id\":1,\"name\":\"Dogs\"},\"photoUrls\":[],\"tags\":[],\"status\":\"available\"}")
                .build());

        Pet createdPet = petApi.addPet(new Pet(null, "Name", new Category(1L, "Name"), List.of(), List.of(), PetStatus.AVAILABLE));

        assertThat(createdPet.id()).isEqualTo(10L);
        assertThat(createdPet.name()).isEqualTo("Doggo");

        var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getUrl().encodedPath()).isEqualTo("/pet");
    }
}
