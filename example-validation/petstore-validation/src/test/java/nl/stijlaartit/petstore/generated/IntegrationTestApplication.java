package nl.stijlaartit.petstore.generated;

import nl.stijlaartit.petstore.generated.client.PetApi;
import nl.stijlaartit.petstore.generated.client.StoreApi;
import nl.stijlaartit.petstore.generated.client.UserApi;
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
        System.out.println(url);
        RestClient restClient = RestClient.create(url);
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }

    @Bean
    PetApi petApi(HttpServiceProxyFactory factory) {
        return factory.createClient(PetApi.class);
    }

    @Bean
    StoreApi storeApi(HttpServiceProxyFactory factory) {
        return factory.createClient(StoreApi.class);
    }

    @Bean
    UserApi userApi(HttpServiceProxyFactory factory) {
        return factory.createClient(UserApi.class);
    }

}
