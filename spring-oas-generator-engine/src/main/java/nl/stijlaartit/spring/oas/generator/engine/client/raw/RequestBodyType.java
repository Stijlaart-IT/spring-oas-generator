package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import io.swagger.v3.oas.models.media.Schema;

public sealed interface RequestBodyType permits RequestBodyType.Resource, RequestBodyType.None, RequestBodyType.Unknown, RequestBodyType.Typed {

    record Resource() implements RequestBodyType {
    }

    record None() implements RequestBodyType {
    }

    record Unknown() implements RequestBodyType {
    }

    record Typed(Schema schema) implements RequestBodyType {
    }
}
