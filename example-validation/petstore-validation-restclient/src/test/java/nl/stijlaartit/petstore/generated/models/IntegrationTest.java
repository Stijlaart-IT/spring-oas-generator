package nl.stijlaartit.petstore.generated.models;

import nl.stijlaartit.petstore.generated.client.PetApi;
import nl.stijlaartit.petstore.generated.client.StoreApi;
import nl.stijlaartit.petstore.generated.client.UserApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    private PetApi petApi;
    @Autowired
    private StoreApi storeApi;
    @Autowired
    private UserApi userApi;

    @Test
    void shouldBuildApis() {
        Objects.requireNonNull(petApi);
        Objects.requireNonNull(storeApi);
        Objects.requireNonNull(userApi);

    }

    @Test
    void shouldMakeACall() {
        Pet newPet = new Pet(null, "Name", new Category(1L, "Name"), List.of(), List.of(), PetStatus.AVAILABLE);
        ResourceAccessException failure =assertThrows(ResourceAccessException.class, () -> petApi.addPet(newPet));
        assertThat(failure).hasMessage("I/O error on POST request for \"http://localhost:7777/pet\": null");
    }
}
