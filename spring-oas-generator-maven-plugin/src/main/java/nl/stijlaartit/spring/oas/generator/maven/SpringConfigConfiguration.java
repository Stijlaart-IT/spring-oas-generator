package nl.stijlaartit.spring.oas.generator.maven;

import org.apache.maven.plugins.annotations.Parameter;

public final class SpringConfigConfiguration {
    @Parameter
    private String serviceGroupName;

    public String serviceGroupName() {
        return serviceGroupName;
    }
}
