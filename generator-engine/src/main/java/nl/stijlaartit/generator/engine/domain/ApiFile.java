package nl.stijlaartit.generator.engine.domain;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApiFile implements GenerationFile {
    private final String name;
    private final List<OperationModel> operations = new ArrayList<>();

    public ApiFile(String name, @Nullable List<OperationModel> operations) {
        this.name = Objects.requireNonNull(name);
        if (operations != null) {
            this.operations.addAll(operations);
        }
    }

    @Override
    public String name() {
        return name;
    }

    public List<OperationModel> getOperations() {
        return operations;
    }
}
