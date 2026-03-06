package nl.stijlaartit.spring.oas.generator.engine.domain.simplified;

import java.util.Objects;

public sealed interface SimplifiedRequest permits SimplifiedRequest.Json, SimplifiedRequest.Binary {

    record Json(SimpleSchema schema, String mediaType) implements SimplifiedRequest {
        public Json {
            Objects.requireNonNull(schema);
            Objects.requireNonNull(mediaType);
            if (mediaType.isBlank()) {
                throw new IllegalArgumentException("mediaType must not be blank");
            }
        }
    }

    record Binary(String mediaType) implements SimplifiedRequest {
        public Binary {
            Objects.requireNonNull(mediaType);
            if (mediaType.isBlank()) {
                throw new IllegalArgumentException("mediaType must not be blank");
            }
        }
    }
}
