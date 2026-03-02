package nl.stijlaartit.spring.oas.generator.maven;

import org.apache.maven.plugins.annotations.Parameter;

public final class ModelConfiguration {
    @Parameter
    private String builderMode = "STRICT";

    @Parameter
    private boolean disableJacksonRequired = false;

    @Parameter
    private String jacksonVersion = "3";

    public String builderMode() {
        return builderMode;
    }

    public boolean disableJacksonRequired() {
        return disableJacksonRequired;
    }

    public String jacksonVersion() {
        return jacksonVersion;
    }
}
