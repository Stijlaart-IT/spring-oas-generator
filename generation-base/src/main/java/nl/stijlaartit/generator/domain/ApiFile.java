package nl.stijlaartit.generator.domain;

import nl.stijlaartit.generator.model.TypeDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ApiFile {
    private String name;
    private final List<OperationModel> operations = new ArrayList<>();

    public ApiFile() {
    }

    public ApiFile(String name, List<OperationModel> operations) {
        this.name = Objects.requireNonNull(name);
        if (operations != null) {
            this.operations.addAll(operations);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public List<OperationModel> getOperations() {
        return operations;
    }

    public List<String> getDependencies() {
        Set<String> dependencies = new LinkedHashSet<>();
        for (OperationModel operation : operations) {
            collect(operation.getRequestBody(), dependencies);
            collect(operation.getResponseType(), dependencies);
            for (ParameterModel parameter : operation.getParameters()) {
                collect(parameter.getType(), dependencies);
            }
        }
        return List.copyOf(dependencies);
    }

    private void collect(TypeDescriptor type, Set<String> dependencies) {
        if (type == null) {
            return;
        }
        dependencies.addAll(TypeDependencies.collectQualifiedTypes(type));
    }
}
