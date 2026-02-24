package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.Objects;

public record SimpleParam(
        String name,
        ParamIn in,
        SimpleSchema schema,
        boolean required
) {

    public SimpleParam {
        Objects.requireNonNull(name);
        Objects.requireNonNull(in);
        Objects.requireNonNull(schema);
    }
}
