package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import io.swagger.v3.oas.models.media.Schema;

public sealed interface ResponseBodyType permits ResponseBodyType.None, ResponseBodyType.SchemaType {

    record None() implements ResponseBodyType {
    }

    record SchemaType(Schema schema) implements ResponseBodyType {}
}
