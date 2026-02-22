package nl.stijlaartit.spring.oas.generator.domain.file;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApiFile implements GenerationFile {
    private final String name;
    private final List<ApiOperation> operations = new ArrayList<>();

    public ApiFile(String name, @Nullable List<ApiOperation> operations) {
        this.name = Objects.requireNonNull(name);
        if (operations != null) {
            this.operations.addAll(operations);
        }
    }

    @Override
    public String name() {
        return name;
    }

    public List<ApiOperation> getOperations() {
        return operations;
    }
}
