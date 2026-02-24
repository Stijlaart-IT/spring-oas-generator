package nl.stijlaartit.spring.oas.generator.engine.domain.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<NamedPathRoot> isComponentRootPath() {
        return segments.isEmpty() ? isComponentPath() : Optional.empty();
    }

    public Optional<NamedPathRoot> isComponentPath() {
        return switch (root) {
            case PathRoot.ComponentParameter ignored -> Optional.of(ignored);
            case PathRoot.ComponentSchema ignored -> Optional.of(ignored);
            case PathRoot.RequestBody ignored -> Optional.empty();
            case PathRoot.RequestParam ignored -> Optional.empty();
            case PathRoot.ResponseBody ignored -> Optional.empty();
            case PathRoot.SharedPathParam ignored -> Optional.empty();
        };
    }

    public boolean isOperationPath() {
        return switch (root) {
            case PathRoot.ComponentSchema ignored -> false;
            case PathRoot.RequestBody ignored -> true;
            case PathRoot.RequestParam ignored -> true;
            case PathRoot.ResponseBody ignored -> true;
            case PathRoot.ComponentParameter ignored -> false;
            case PathRoot.SharedPathParam ignored -> false;
        };
    }
}
