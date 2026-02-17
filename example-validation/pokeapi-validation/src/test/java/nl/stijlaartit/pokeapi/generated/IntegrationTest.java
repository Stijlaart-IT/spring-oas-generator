package nl.stijlaartit.pokeapi.generated;

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
    private BerriesApi berriesApi;
    @Autowired
    private ContestsApi contestsApi;
    @Autowired
    private EncountersApi encountersApi;
    @Autowired
    private EvolutionApi evolutionApi;
    @Autowired
    private GamesApi gamesApi;
    @Autowired
    private ItemsApi itemsApi;
    @Autowired
    private LocationApi locationApi;
    @Autowired
    private MachinesApi machinesApi;
    @Autowired
    private MovesApi movesApi;
    @Autowired
    private PokemonApi pokemonApi;
    @Autowired
    private UtilityApi utilityApi;

    @Test
    void shouldBuildApis() {
        Objects.requireNonNull(berriesApi);
        Objects.requireNonNull(contestsApi);
        Objects.requireNonNull(encountersApi);
        Objects.requireNonNull(evolutionApi);
        Objects.requireNonNull(gamesApi);
        Objects.requireNonNull(itemsApi);
        Objects.requireNonNull(locationApi);
        Objects.requireNonNull(machinesApi);
        Objects.requireNonNull(movesApi);
        Objects.requireNonNull(pokemonApi);
        Objects.requireNonNull(utilityApi);
    }

    @Test
    void shouldMakeACall() {
        ResourceAccessException failure = assertThrows(
                ResourceAccessException.class,
                () -> pokemonApi.apiV2PokemonList(20, 0, null)
        );
        assertThat(failure.getMessage())
                .startsWith("I/O error on GET request for \"http://localhost:7777/api/v2/pokemon/\"");
    }
}
