package nl.stijlaartit.generator.engine.domain;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public sealed interface OperationName permits OperationName.Id, OperationName.PathAndMethod {

    static OperationName id(String s) {
        return new Id(Objects.requireNonNull(s));
    }

    static OperationName pathAndMethod(String path, HttpMethod method) {
        return new PathAndMethod(Objects.requireNonNull(path), Objects.requireNonNull(method));
    }

    record Id(String value) implements OperationName {

    }

    record PathAndMethod(String path, HttpMethod method) implements OperationName {
    }
}
