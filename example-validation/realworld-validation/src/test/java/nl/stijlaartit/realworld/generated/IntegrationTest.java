package nl.stijlaartit.realworld.generated;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import nl.stijlaartit.realworld.generated.client.ArticlesApi;
import nl.stijlaartit.realworld.generated.client.CommentsApi;
import nl.stijlaartit.realworld.generated.client.FavoritesApi;
import nl.stijlaartit.realworld.generated.client.ProfileApi;
import nl.stijlaartit.realworld.generated.client.TagsApi;
import nl.stijlaartit.realworld.generated.client.UserAndAuthenticationApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import nl.stijlaartit.realworld.generated.models.UpdateCurrentUserRequest;
import nl.stijlaartit.realworld.generated.models.UpdateUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class IntegrationTest {

    private static MockWebServer server;

    @Autowired
    private ArticlesApi articlesApi;
    @Autowired
    private CommentsApi commentsApi;
    @Autowired
    private FavoritesApi favoritesApi;
    @Autowired
    private ProfileApi profileApi;
    @Autowired
    private TagsApi tagsApi;
    @Autowired
    private UserAndAuthenticationApi userAndAuthenticationApi;

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
        Objects.requireNonNull(articlesApi);
        Objects.requireNonNull(commentsApi);
        Objects.requireNonNull(favoritesApi);
        Objects.requireNonNull(profileApi);
        Objects.requireNonNull(tagsApi);
        Objects.requireNonNull(userAndAuthenticationApi);
    }

    @Test
    void shouldMakeACall() throws InterruptedException {
        server.enqueue(new MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("{\"tags\":[\"spring\",\"java\"]}")
                .build());

        Object response = tagsApi.getTags();
        assertThat(response).isNotNull();

        final var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getUrl().encodedPath()).isEqualTo("/tags");
    }

    @Test
    void shouldResolveTypesFromRequestBodyRefs() throws InterruptedException {
        server.enqueue(new MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("{\"user\":{\"email\":\"jane@example.com\",\"token\":\"jwt-token\",\"username\":\"jane\",\"bio\":null,\"image\":null}}")
                .build());

        // Validates that request body is accepted
        UpdateCurrentUserRequest payload = UpdateCurrentUserRequest.builder()
                .user(UpdateUser.builder()
                        .build())
                .build();

        Object response = userAndAuthenticationApi.updateCurrentUser(payload);
        assertThat(response).isNotNull();

        final var request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getHeaders().get("Content-Type")).isEqualTo("application/json");
        assertThat(request.getUrl().encodedPath()).isEqualTo("/user");
        assertThat(request.getBody().utf8()).isEqualTo("{\"user\":{}}");
    }
}
