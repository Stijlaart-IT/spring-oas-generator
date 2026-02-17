package nl.stijlaartit.spotify.generated;

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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class IntegrationTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationTestApplication.class, args);
    }

    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory() {
        RestClient restClient = RestClient.create("http://localhost:7777");
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }

    @Bean
    AlbumsApi albumsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(AlbumsApi.class);
    }

    @Bean
    ArtistsApi artistsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ArtistsApi.class);
    }

    @Bean
    AudiobooksApi audiobooksApi(HttpServiceProxyFactory factory) {
        return factory.createClient(AudiobooksApi.class);
    }

    @Bean
    CategoriesApi categoriesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(CategoriesApi.class);
    }

    @Bean
    ChaptersApi chaptersApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ChaptersApi.class);
    }

    @Bean
    EpisodesApi episodesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(EpisodesApi.class);
    }

    @Bean
    GenresApi genresApi(HttpServiceProxyFactory factory) {
        return factory.createClient(GenresApi.class);
    }

    @Bean
    MarketsApi marketsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(MarketsApi.class);
    }

    @Bean
    PlayerApi playerApi(HttpServiceProxyFactory factory) {
        return factory.createClient(PlayerApi.class);
    }

    @Bean
    PlaylistsApi playlistsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(PlaylistsApi.class);
    }

    @Bean
    SearchApi searchApi(HttpServiceProxyFactory factory) {
        return factory.createClient(SearchApi.class);
    }

    @Bean
    ShowsApi showsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ShowsApi.class);
    }

    @Bean
    TracksApi tracksApi(HttpServiceProxyFactory factory) {
        return factory.createClient(TracksApi.class);
    }

    @Bean
    UsersApi usersApi(HttpServiceProxyFactory factory) {
        return factory.createClient(UsersApi.class);
    }
}
