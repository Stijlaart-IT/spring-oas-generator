package nl.stijlaartit.spring.oas.generator.engine.domain;

import nl.stijlaartit.spring.oas.generator.engine.model.TypeDescriptor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record OperationModel(OperationName name, HttpMethod method, String path, List<ParameterModel> parameters,
                             @Nullable TypeDescriptor requestBody, @Nullable TypeDescriptor responseType,
                             boolean deprecated) {
    public OperationModel(OperationName name,
                          HttpMethod method,
                          String path,
                          List<ParameterModel> parameters,
                          @Nullable TypeDescriptor requestBody,
                          @Nullable TypeDescriptor responseType,
                          boolean deprecated) {
        this.name = Objects.requireNonNull(name);
        this.method = Objects.requireNonNull(method);
        this.path = Objects.requireNonNull(path);
        this.parameters = List.copyOf(parameters);
        this.requestBody = requestBody;
        this.responseType = responseType;
        this.deprecated = deprecated;
    }
}
