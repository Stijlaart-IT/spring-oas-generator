package nl.stijlaartit.spring.oas.generator.domain.file;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record ApiOperation(JavaMethodName name, ApiHttpMethod method, String path, List<ParameterModel> parameters,
                           @Nullable TypeDescriptor requestBody, @Nullable TypeDescriptor responseType,
                           boolean deprecated) {
    public ApiOperation(JavaMethodName name,
                        ApiHttpMethod method,
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
