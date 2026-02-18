package nl.stijlaartit.spring.oas.generator.engine.model;

public record RecordModelWriterConfig(BuilderMode builderMode) {

    public static RecordModelWriterConfig defaultConfig() {
        return new RecordModelWriterConfig(BuilderMode.STRICT);
    }

    public enum BuilderMode {
        DISABLED,
        STRICT,
        RELAXED
    }
}
