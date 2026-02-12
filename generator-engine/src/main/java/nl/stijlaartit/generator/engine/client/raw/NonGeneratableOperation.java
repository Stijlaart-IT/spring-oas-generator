package nl.stijlaartit.generator.engine.client.raw;

import org.jspecify.annotations.Nullable;

public record NonGeneratableOperation(String path,
                                      io.swagger.v3.oas.models.PathItem.HttpMethod key,
                                      @Nullable String operationId,
                                      String methodHeadNotSupported) implements RawOperation {
}
