package nl.stijlaartit.spring.oas.generator.engine;

import java.util.Objects;

public record SpringConfigGenerationConfig(String serviceGroupName) {

    public SpringConfigGenerationConfig {
        Objects.requireNonNull(serviceGroupName, "serviceGroupName");
        serviceGroupName = serviceGroupName.trim();
        if (serviceGroupName.isBlank()) {
            throw new IllegalArgumentException("serviceGroupName must not be blank");
        }
    }
}
