package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import io.swagger.v3.oas.models.PathItem;
import org.jspecify.annotations.Nullable;

public record NonGeneratableOperation(String path,
                                      PathItem.HttpMethod method,
                                      @Nullable String operationId,
                                      String message) implements RawOperation {
}
