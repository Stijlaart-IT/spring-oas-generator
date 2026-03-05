package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.ResponseMediaType;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;

public sealed interface ResponseBodyType permits ResponseBodyType.None, ResponseBodyType.SchemaType {

    record None() implements ResponseBodyType {
    }

    record SchemaType(SimpleSchema schema, ResponseMediaType mediaType) implements ResponseBodyType {}
}
