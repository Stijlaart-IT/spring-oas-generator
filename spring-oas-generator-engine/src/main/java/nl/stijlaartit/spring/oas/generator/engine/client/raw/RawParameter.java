package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;

public record RawParameter(

        String name,
        ParameterLocation location,
        SimpleSchema schema,
        boolean required

) {

    public enum ParameterLocation {
        PATH, QUERY, HEADER
    }
}
