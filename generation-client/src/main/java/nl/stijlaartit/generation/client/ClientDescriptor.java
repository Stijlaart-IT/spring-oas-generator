package nl.stijlaartit.generation.client;

import java.util.List;
import java.util.Objects;

public record ClientDescriptor(
        String name,
        List<OperationDescriptor> operations
) {
    public ClientDescriptor {
        Objects.requireNonNull(name);
        operations = List.copyOf(operations);
    }
}
