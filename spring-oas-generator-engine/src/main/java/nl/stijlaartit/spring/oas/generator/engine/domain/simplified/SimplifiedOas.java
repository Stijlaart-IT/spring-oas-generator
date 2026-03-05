package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SimplifiedOas(
        Map<String, SimpleSchema> componentSchema,
        Map<String, SimpleSchema> componentResponses,
        Map<String, SimpleSchema> componentParameters,
        List<SimplifiedOperation> operations,
        Map<String, List<SimpleParam>> pathParams
) {
    public SimplifiedOas {
        Objects.requireNonNull(componentSchema);
        Objects.requireNonNull(componentResponses);
        Objects.requireNonNull(componentParameters);
        Objects.requireNonNull(operations);
        Objects.requireNonNull(pathParams);
    }
}
