package nl.stijlaartit.spring.oas.generator.engine.domain.path;

import java.util.ArrayList;
import java.util.List;

public record SchemaPath(PathRoot root, List<PathSegment> segments) {

    public static SchemaPath forRoot(PathRoot pathRoot) {
        return new SchemaPath(pathRoot, List.of());
    }


    public SchemaPath variant(String allOf, int i) {
        final var segments = new ArrayList<>(this.segments);
        segments.add(PathSegment.variant(allOf, i));
        return new SchemaPath(root, segments);
    }

    public SchemaPath singletonVariant(String allOf) {
        final var segments = new ArrayList<>(this.segments);
        segments.add(PathSegment.singletonVariant(allOf));
        return new SchemaPath(root, segments);
    }

    public SchemaPath property(String key) {
        final var segments = new ArrayList<>(this.segments);
        segments.add(PathSegment.property(key));
        return new SchemaPath(root, segments);
    }

    public SchemaPath items() {
        final var segments = new ArrayList<>(this.segments);
        segments.add(PathSegment.items());
        return new SchemaPath(root, segments);
    }

    public SchemaPath additionalProperties() {
        final var segments = new ArrayList<>(this.segments);
        segments.add(PathSegment.additionalProperties());
        return new SchemaPath(root, segments);
    }
}
