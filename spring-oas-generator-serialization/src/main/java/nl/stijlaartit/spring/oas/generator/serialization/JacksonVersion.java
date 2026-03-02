package nl.stijlaartit.spring.oas.generator.serialization;

import java.util.Optional;

public enum JacksonVersion {
    V2("2"),
    V3("3");

    private final String value;

    JacksonVersion(String value) {
        this.value = value;
    }

    public static Optional<JacksonVersion> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String trimmed = value.trim();
        for (JacksonVersion jacksonVersion : values()) {
            if (jacksonVersion.value.equals(trimmed)) {
                return Optional.of(jacksonVersion);
            }
        }
        return Optional.empty();
    }
}
