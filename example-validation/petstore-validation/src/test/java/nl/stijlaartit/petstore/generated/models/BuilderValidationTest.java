package nl.stijlaartit.petstore.generated.models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuilderValidationTest {

    @Test
    void buildsPetWithBuilder() {
        var category = new Category(1L, "Dogs");

        var pet = Pet.builder()
                .id(10L)
                .name("Doggo")
                .photoUrls(List.of("http://example.com/photo1.jpg"))
                .category(category)
                .tags(List.of(new Tag(0L, "friendly")))
                .status(PetStatus.AVAILABLE)
                .build();

        assertThat(pet).isNotNull();
        assertThat(pet.id()).isEqualTo(10L);
        assertThat(pet.name()).isEqualTo("Doggo");
        assertThat(pet.photoUrls()).containsExactly("http://example.com/photo1.jpg");
        assertThat(pet.category()).isEqualTo(category);
        assertThat(pet.tags()).extracting(Tag::name).containsExactly("friendly");
        assertThat(pet.status()).isEqualTo(PetStatus.AVAILABLE);
    }

    @Test
    void builderRejectsNullForNonNullableField() {
        assertThrows(NullPointerException.class, () -> Pet.builder()
                .name(null)
                .photoUrls(List.of("http://example.com/photo1.jpg"))
                .build());
    }
}
