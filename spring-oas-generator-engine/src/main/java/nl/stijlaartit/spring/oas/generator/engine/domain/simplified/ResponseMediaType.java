package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.Locale;

public enum ResponseMediaType {
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    UNKNOWN("");

    private final String value;

    ResponseMediaType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    public static ResponseMediaType from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "application/json" -> APPLICATION_JSON;
            case "application/octet-stream" -> APPLICATION_OCTET_STREAM;
            default -> UNKNOWN;
        };
    }
}
