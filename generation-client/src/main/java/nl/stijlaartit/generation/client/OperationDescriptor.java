package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.model.TypeDescriptor;

import java.util.List;
import java.util.Objects;

public record OperationDescriptor(
        String name,
        HttpMethod method,
        String path,
        List<ParameterDescriptor> parameters,
        TypeDescriptor requestBody,
        TypeDescriptor responseType
) {
    public OperationDescriptor {
        Objects.requireNonNull(name);
        Objects.requireNonNull(method);
        Objects.requireNonNull(path);
        parameters = List.copyOf(parameters);
    }

    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }
}
