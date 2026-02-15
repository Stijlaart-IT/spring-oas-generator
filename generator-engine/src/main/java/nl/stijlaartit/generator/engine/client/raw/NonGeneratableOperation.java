package nl.stijlaartit.generator.engine.client.raw;

import io.swagger.v3.oas.models.PathItem;
import org.jspecify.annotations.Nullable;

public record NonGeneratableOperation(String path,
                                      PathItem.HttpMethod key,
                                      @Nullable String operationId,
                                      String methodHeadNotSupported) implements RawOperation {
}
