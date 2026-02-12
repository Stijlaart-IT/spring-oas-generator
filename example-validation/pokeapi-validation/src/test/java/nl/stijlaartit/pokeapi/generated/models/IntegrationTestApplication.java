package nl.stijlaartit.pokeapi.generated.models;

import nl.stijlaartit.pokeapi.generated.client.BerriesApi;
import nl.stijlaartit.pokeapi.generated.client.ContestsApi;
import nl.stijlaartit.pokeapi.generated.client.EncountersApi;
import nl.stijlaartit.pokeapi.generated.client.EvolutionApi;
import nl.stijlaartit.pokeapi.generated.client.GamesApi;
import nl.stijlaartit.pokeapi.generated.client.ItemsApi;
import nl.stijlaartit.pokeapi.generated.client.LocationApi;
import nl.stijlaartit.pokeapi.generated.client.MachinesApi;
import nl.stijlaartit.pokeapi.generated.client.MovesApi;
import nl.stijlaartit.pokeapi.generated.client.PokemonApi;
import nl.stijlaartit.pokeapi.generated.client.UtilityApi;
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
    BerriesApi berriesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(BerriesApi.class);
    }

    @Bean
    ContestsApi contestsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ContestsApi.class);
    }

    @Bean
    EncountersApi encountersApi(HttpServiceProxyFactory factory) {
        return factory.createClient(EncountersApi.class);
    }

    @Bean
    EvolutionApi evolutionApi(HttpServiceProxyFactory factory) {
        return factory.createClient(EvolutionApi.class);
    }

    @Bean
    GamesApi gamesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(GamesApi.class);
    }

    @Bean
    ItemsApi itemsApi(HttpServiceProxyFactory factory) {
        return factory.createClient(ItemsApi.class);
    }

    @Bean
    LocationApi locationApi(HttpServiceProxyFactory factory) {
        return factory.createClient(LocationApi.class);
    }

    @Bean
    MachinesApi machinesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(MachinesApi.class);
    }

    @Bean
    MovesApi movesApi(HttpServiceProxyFactory factory) {
        return factory.createClient(MovesApi.class);
    }

    @Bean
    PokemonApi pokemonApi(HttpServiceProxyFactory factory) {
        return factory.createClient(PokemonApi.class);
    }

    @Bean
    UtilityApi utilityApi(HttpServiceProxyFactory factory) {
        return factory.createClient(UtilityApi.class);
    }
}
