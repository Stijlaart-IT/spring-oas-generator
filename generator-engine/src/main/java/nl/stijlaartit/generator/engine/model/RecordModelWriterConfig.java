package nl.stijlaartit.generator.engine.model;

public final class RecordModelWriterConfig {
    private final boolean generateBuilders;
    private final boolean disableBuilderStrictMode;

    public RecordModelWriterConfig(boolean generateBuilders, boolean disableBuilderStrictMode) {
        this.generateBuilders = generateBuilders;
        this.disableBuilderStrictMode = disableBuilderStrictMode;
    }

    public static RecordModelWriterConfig defaultConfig() {
        return new RecordModelWriterConfig(true, false);
    }

    public boolean generateBuilders() {
        return generateBuilders;
    }

    public boolean disableBuilderStrictMode() {
        return disableBuilderStrictMode;
    }
}
