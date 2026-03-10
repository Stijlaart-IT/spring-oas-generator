package nl.stijlaartit.petstore.generated;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import nl.stijlaartit.petstore.generated.client.PetApi;
import nl.stijlaartit.petstore.generated.client.StoreApi;
import nl.stijlaartit.petstore.generated.client.UserApi;
import nl.stijlaartit.petstore.generated.models.Category;
import nl.stijlaartit.petstore.generated.models.Pet;
import nl.stijlaartit.petstore.generated.models.PetStatus;
import nl.stijlaartit.petstore.generated.models.Tag;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class IntegrationTest {

    private static MockWebServer server;

    @Autowired
    private PetApi petApi;
    @Autowired
    private StoreApi storeApi;
    @Autowired
    private UserApi userApi;

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
    void shouldBuildApis() {
        Objects.requireNonNull(petApi);
        Objects.requireNonNull(storeApi);
        Objects.requireNonNull(userApi);

    }

    @Test
    void shouldMakeACall() throws InterruptedException {
        server.enqueue(new MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("{\"id\": 10, \"name\": \"Doggo\", \"category\": {\"id\": 1, \"name\": \"Dogs\"}, \"photoUrls\": [\"http://example.com/photo1.jpg\"], \"tags\": [{\"id\": 0, \"name\": \"friendly\"}], \"status\": \"available\"}")
                .build());

        Pet newPet = new Pet(null, "Name", new Category(1L, "Name"), List.of(), List.of(), PetStatus.AVAILABLE);
        Pet createdPet = petApi.addPet(newPet);

        assertThat(createdPet).isEqualTo(new Pet(10L, "Doggo", new Category(1L, "Dogs"),
                List.of("http://example.com/photo1.jpg"), List.of(new Tag(0L, "friendly")), PetStatus.AVAILABLE));

        final var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().get("Content-Type")).isEqualTo("application/json");
        assertThat(request.getUrl().encodedPath()).isEqualTo("/pet");
        assertThat(request.getBody().utf8()).isEqualTo("{\"name\":\"Name\",\"category\":{\"id\":1,\"name\":\"Name\"},\"photoUrls\":[],\"tags\":[],\"status\":\"available\"}");
    }

    @Test
    void shouldMakeACallResponseEntity() throws InterruptedException {
        server.enqueue(new MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("{\"id\": 10, \"name\": \"Doggo\", \"category\": {\"id\": 1, \"name\": \"Dogs\"}, \"photoUrls\": [\"http://example.com/photo1.jpg\"], \"tags\": [{\"id\": 0, \"name\": \"friendly\"}], \"status\": \"available\"}")
                .build());

        Pet newPet = new Pet(null, "Name", new Category(1L, "Name"), List.of(), List.of(), PetStatus.AVAILABLE);
        ResponseEntity<Pet> response = petApi.addPetResponseEntity(newPet);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new Pet(10L, "Doggo", new Category(1L, "Dogs"),
                List.of("http://example.com/photo1.jpg"), List.of(new Tag(0L, "friendly")), PetStatus.AVAILABLE));

        final var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().get("Content-Type")).isEqualTo("application/json");
        assertThat(request.getUrl().encodedPath()).isEqualTo("/pet");
        assertThat(request.getBody().utf8()).isEqualTo("{\"name\":\"Name\",\"category\":{\"id\":1,\"name\":\"Name\"},\"photoUrls\":[],\"tags\":[],\"status\":\"available\"}");
    }
}
