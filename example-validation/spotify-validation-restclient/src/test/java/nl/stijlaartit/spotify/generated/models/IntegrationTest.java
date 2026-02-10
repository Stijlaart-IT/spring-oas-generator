package nl.stijlaartit.spotify.generated.models;

import nl.stijlaartit.spotify.generated.client.AlbumsApi;
import nl.stijlaartit.spotify.generated.client.ArtistsApi;
import nl.stijlaartit.spotify.generated.client.AudiobooksApi;
import nl.stijlaartit.spotify.generated.client.CategoriesApi;
import nl.stijlaartit.spotify.generated.client.ChaptersApi;
import nl.stijlaartit.spotify.generated.client.EpisodesApi;
import nl.stijlaartit.spotify.generated.client.GenresApi;
import nl.stijlaartit.spotify.generated.client.MarketsApi;
import nl.stijlaartit.spotify.generated.client.PlayerApi;
import nl.stijlaartit.spotify.generated.client.PlaylistsApi;
import nl.stijlaartit.spotify.generated.client.SearchApi;
import nl.stijlaartit.spotify.generated.client.ShowsApi;
import nl.stijlaartit.spotify.generated.client.TracksApi;
import nl.stijlaartit.spotify.generated.client.UsersApi;
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
    private AlbumsApi albumsApi;
    @Autowired
    private ArtistsApi artistsApi;
    @Autowired
    private AudiobooksApi audiobooksApi;
    @Autowired
    private CategoriesApi categoriesApi;
    @Autowired
    private ChaptersApi chaptersApi;
    @Autowired
    private EpisodesApi episodesApi;
    @Autowired
    private GenresApi genresApi;
    @Autowired
    private MarketsApi marketsApi;
    @Autowired
    private PlayerApi playerApi;
    @Autowired
    private PlaylistsApi playlistsApi;
    @Autowired
    private SearchApi searchApi;
    @Autowired
    private ShowsApi showsApi;
    @Autowired
    private TracksApi tracksApi;
    @Autowired
    private UsersApi usersApi;

    @Test
    void shouldBuildApis() {
        Objects.requireNonNull(albumsApi);
        Objects.requireNonNull(artistsApi);
        Objects.requireNonNull(audiobooksApi);
        Objects.requireNonNull(categoriesApi);
        Objects.requireNonNull(chaptersApi);
        Objects.requireNonNull(episodesApi);
        Objects.requireNonNull(genresApi);
        Objects.requireNonNull(marketsApi);
        Objects.requireNonNull(playerApi);
        Objects.requireNonNull(playlistsApi);
        Objects.requireNonNull(searchApi);
        Objects.requireNonNull(showsApi);
        Objects.requireNonNull(tracksApi);
        Objects.requireNonNull(usersApi);
    }

    @Test
    void shouldMakeACall() {
        ResourceAccessException failure = assertThrows(
                ResourceAccessException.class,
                () -> albumsApi.getNewReleases(20, 0)
        );
        assertThat(failure.getMessage())
                .startsWith("I/O error on GET request for \"http://localhost:7777/browse/new-releases\":");
    }
}
