package nl.stijlaartit.session.generated.models;

import nl.stijlaartit.session.generated.client.AuthApi;
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
    AuthApi authApi(HttpServiceProxyFactory factory) {
        return factory.createClient(AuthApi.class);
    }
}
