package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import io.swagger.v3.oas.models.media.Schema;

public record RawParameter(

        String name,
        ParameterLocation location,
        Schema schema,
        boolean required

) {

    public enum ParameterLocation {
        PATH, QUERY, HEADER
    }
}
