package nl.stijlaartit.spring.oas.generator.engine.domain;

public record SchemaRef(String type, String name) {

    public static SchemaRef parseFromRefValue(String refValue) {
        if (!refValue.startsWith("#/components/")) {
            throw new IllegalArgumentException("Invalid component schema reference: " + refValue);
        }
        final var name = refValue.replaceFirst("#/components/", "");
        final var parts = name.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid component schema reference: " + refValue);
        }
        return new SchemaRef(parts[0], parts[1]);
    }
}
