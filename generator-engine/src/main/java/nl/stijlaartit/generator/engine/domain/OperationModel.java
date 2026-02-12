package nl.stijlaartit.generator.engine.domain;

import nl.stijlaartit.generator.engine.model.TypeDescriptor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class OperationModel {
    private OperationName name;
    private HttpMethod method;
    private String path;
    private final List<ParameterModel> parameters;
    @Nullable
    private final TypeDescriptor requestBody;
    @Nullable
    private final TypeDescriptor responseType;
    private final boolean deprecated;

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

    public OperationName getName() {
        return name;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = Objects.requireNonNull(method);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = Objects.requireNonNull(path);
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    @Nullable
    public TypeDescriptor getRequestBody() {
        return requestBody;
    }

    @Nullable
    public TypeDescriptor getResponseType() {
        return responseType;
    }

    public boolean isDeprecated() {
        return deprecated;
    }
}
