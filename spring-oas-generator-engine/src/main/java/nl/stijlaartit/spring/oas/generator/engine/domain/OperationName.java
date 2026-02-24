package nl.stijlaartit.spring.oas.generator.engine.domain;

import java.util.Objects;

public sealed interface OperationName permits OperationName.Id, OperationName.PathAndMethod {

    String format();

    static OperationName id(String s) {
        return new Id(Objects.requireNonNull(s));
    }

    static OperationName pathAndMethod(String path, HttpMethod method) {
        return new PathAndMethod(Objects.requireNonNull(path), Objects.requireNonNull(method));
    }

    record Id(String value) implements OperationName {

        @Override
        public String format() {
            return value;
        }
    }

    record PathAndMethod(String path, HttpMethod method) implements OperationName {
        @Override
        public String format() {
            return String.format("%s %s", method.name(), path);
        }
    }
}
