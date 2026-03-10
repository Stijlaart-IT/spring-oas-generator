package nl.stijlaartit.realworld.generated;

import nl.stijlaartit.realworld.generated.client.ArticlesApi;
import nl.stijlaartit.realworld.generated.client.CommentsApi;
import nl.stijlaartit.realworld.generated.client.FavoritesApi;
import nl.stijlaartit.realworld.generated.client.ProfileApi;
import nl.stijlaartit.realworld.generated.client.TagsApi;
import nl.stijlaartit.realworld.generated.client.UserAndAuthenticationApi;
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
        String url = System.getProperty("mockserver.url");
        RestClient restClient = RestClient.create(url);
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }

    @Bean
    ArticlesApi articlesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ArticlesApi.class);
    }

    @Bean
    CommentsApi commentsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(CommentsApi.class);
    }

    @Bean
    FavoritesApi favoritesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(FavoritesApi.class);
    }

    @Bean
    ProfileApi profileApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ProfileApi.class);
    }

    @Bean
    TagsApi tagsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(TagsApi.class);
    }

    @Bean
    UserAndAuthenticationApi userAndAuthenticationApi(HttpServiceProxyFactory factory) {
        return factory.createClient(UserAndAuthenticationApi.class);
    }
}
