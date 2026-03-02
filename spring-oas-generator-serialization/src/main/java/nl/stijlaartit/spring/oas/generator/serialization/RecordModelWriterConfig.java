package nl.stijlaartit.spring.oas.generator.serialization;

import java.util.Objects;

public record RecordModelWriterConfig(BuilderMode builderMode, boolean disableJacksonRequired) {

    public RecordModelWriterConfig {
        Objects.requireNonNull(builderMode, "builderMode");
    }

    public RecordModelWriterConfig(BuilderMode builderMode) {
        this(builderMode, false);
    }

    public static RecordModelWriterConfig defaultConfig() {
        return new RecordModelWriterConfig(BuilderMode.STRICT, false);
    }
}
