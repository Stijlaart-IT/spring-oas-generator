package nl.stijlaartit.spring.oas.generator.engine.client.raw;

import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimpleSchema;

public sealed interface RequestBodyType permits RequestBodyType.Resource, RequestBodyType.None, RequestBodyType.Unknown, RequestBodyType.Typed {

    record Resource(String mediaType) implements RequestBodyType {
    }

    record None() implements RequestBodyType {
    }

    record Unknown() implements RequestBodyType {
    }

    record Typed(SimpleSchema schema, String mediaType) implements RequestBodyType {
    }
}
