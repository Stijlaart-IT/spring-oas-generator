package nl.stijlaartit.spring.oas.generator.domain.file;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public record ApiOperation(JavaMethodName name, ApiHttpMethod method, String path, List<ParameterModel> parameters,
                           @Nullable TypeDescriptor requestBody, @Nullable TypeDescriptor responseType,
                           @Nullable String contentType,
                           @Nullable String accept,
                           boolean deprecated) {
    public ApiOperation(JavaMethodName name,
                        ApiHttpMethod method,
                        String path,
                        List<ParameterModel> parameters,
                        @Nullable TypeDescriptor requestBody,
                        @Nullable TypeDescriptor responseType,
                        @Nullable String contentType,
                        @Nullable String accept,
                        boolean deprecated) {
        this.name = Objects.requireNonNull(name);
        this.method = Objects.requireNonNull(method);
        this.path = Objects.requireNonNull(path);
        this.parameters = List.copyOf(parameters);
        this.requestBody = requestBody;
        this.responseType = responseType;
        this.contentType = contentType;
        this.accept = accept;
        this.deprecated = deprecated;
    }

    public ApiOperation(JavaMethodName name,
                        ApiHttpMethod method,
                        String path,
                        List<ParameterModel> parameters,
                        @Nullable TypeDescriptor requestBody,
                        @Nullable TypeDescriptor responseType,
                        boolean deprecated) {
        this(name, method, path, parameters, requestBody, responseType, null, null, deprecated);
    }

    public ApiOperation(JavaMethodName name,
                        ApiHttpMethod method,
                        String path,
                        List<ParameterModel> parameters,
                        @Nullable TypeDescriptor requestBody,
                        @Nullable TypeDescriptor responseType,
                        @Nullable String accept,
                        boolean deprecated) {
        this(name, method, path, parameters, requestBody, responseType, null, accept, deprecated);
    }
}
