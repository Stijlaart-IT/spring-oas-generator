package nl.stijlaartit.spring.oas.generator.engine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringConfigGenerationConfigTest {

    @Test
    void trimsServiceGroupName() {
        SpringConfigGenerationConfig config = new SpringConfigGenerationConfig("  petstore  ");
        assertThat(config.serviceGroupName()).isEqualTo("petstore");
    }

    @Test
    void rejectsBlankServiceGroupName() {
        assertThatThrownBy(() -> new SpringConfigGenerationConfig("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("serviceGroupName");
    }
}
