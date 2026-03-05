package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.Objects;

public record SimpleReponse(
        String status,
        SimpleSchema schema,
        ResponseMediaType mediaType
) {

    public SimpleReponse {
        Objects.requireNonNull(status);
        Objects.requireNonNull(schema);
        Objects.requireNonNull(mediaType);
    }
}
