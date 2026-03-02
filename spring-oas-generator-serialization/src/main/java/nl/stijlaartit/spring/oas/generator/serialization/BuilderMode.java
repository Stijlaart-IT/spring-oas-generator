package nl.stijlaartit.spring.oas.generator.serialization;

import java.util.Locale;
import java.util.Optional;

public enum BuilderMode {
    DISABLED,
    STRICT,
    RELAXED;

    public static Optional<BuilderMode> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(BuilderMode.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
