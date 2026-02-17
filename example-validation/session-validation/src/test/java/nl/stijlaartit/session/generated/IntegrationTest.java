package nl.stijlaartit.session.generated;

import nl.stijlaartit.session.generated.client.AuthApi;
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
    private AuthApi authApi;

    @Test
    void shouldBuildApis() {
        Objects.requireNonNull(authApi);
    }

    @Test
    void shouldMakeACall() {
        ResourceAccessException failure = assertThrows(
                ResourceAccessException.class,
                authApi::getSession
        );
        assertThat(failure.getMessage())
                .startsWith("I/O error on GET request for \"http://localhost:7777/auth/session\":");
    }
}
