package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import org.jspecify.annotations.Nullable;

public record SimplifiedOperation(
        String path,
        HttpMethod method,
        @Nullable String operationId,
        Set<String> tags,
        List<SimpleParam> params,
        List<SimpleReponse> responses,
        @Nullable SimpleSchema requestBody
) {

    public SimplifiedOperation {
        Objects.requireNonNull(path);
        Objects.requireNonNull(method);
        Objects.requireNonNull(tags);
        Objects.requireNonNull(params);
        Objects.requireNonNull(responses);
    }
}
