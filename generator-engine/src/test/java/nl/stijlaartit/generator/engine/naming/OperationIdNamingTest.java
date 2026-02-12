package nl.stijlaartit.generator.engine.naming;

import nl.stijlaartit.generator.engine.domain.HttpMethod;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationIdNamingTest {

    @Test
    void fallsBackWithHttpMethodAndPath() {
        assertEquals("get_pets_petId", OperationIdNaming.fallbackOperationId(
                PathItem.HttpMethod.GET,
                "/pets/{petId}/"
        ));
    }

    @Test
    void fallsBackWithDomainHttpMethod() {
        assertEquals("post_orders", OperationIdNaming.fallbackOperationId(
                HttpMethod.POST,
                "/orders"
        ));
    }

    @Test
    void fallsBackWhenMethodOrPathMissing() {
        assertEquals("operation", OperationIdNaming.fallbackOperationId((PathItem.HttpMethod) null, null));
        assertEquals("get", OperationIdNaming.fallbackOperationId(PathItem.HttpMethod.GET, ""));
    }
}
