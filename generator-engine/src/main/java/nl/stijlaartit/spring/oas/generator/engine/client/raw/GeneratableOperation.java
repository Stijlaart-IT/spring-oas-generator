package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record GeneratableOperation(
        String path,
        HttpMethod method,
        @Nullable  String operationId,
        List<RawParameter> parameters,
        RequestBodyType requestBodyType,
        ResponseBodyType responseBodyType,
        List<String> tags,
        boolean deprecated
) implements RawOperation {
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
}
