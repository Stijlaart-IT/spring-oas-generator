package nl.stijlaartit.realworld.generated.models;

import nl.stijlaartit.realworld.generated.client.ArticlesApi;
import nl.stijlaartit.realworld.generated.client.CommentsApi;
import nl.stijlaartit.realworld.generated.client.FavoritesApi;
import nl.stijlaartit.realworld.generated.client.ProfileApi;
import nl.stijlaartit.realworld.generated.client.TagsApi;
import nl.stijlaartit.realworld.generated.client.UserAndAuthenticationApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.ResourceAccessException;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class IntegrationTest {

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
    void shouldMakeACall() {
        ResourceAccessException failure = assertThrows(ResourceAccessException.class, tagsApi::GetTags);
        assertThat(failure).hasMessage("I/O error on GET request for \"http://localhost:7777/tags\": null");
    }
}
