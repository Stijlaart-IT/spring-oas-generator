package nl.stijlaartit.generator.domain;

import nl.stijlaartit.generator.model.TypeDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OperationModel {
    private String name;
    private HttpMethod method;
    private String path;
    private final List<ParameterModel> parameters = new ArrayList<>();
    private TypeDescriptor requestBody;
    private TypeDescriptor responseType;
    private boolean deprecated;

    public OperationModel() {
    }

    public OperationModel(String name, HttpMethod method, String path,
                          List<ParameterModel> parameters,
                          TypeDescriptor requestBody,
                          TypeDescriptor responseType,
                          boolean deprecated) {
        this.name = Objects.requireNonNull(name);
        this.method = Objects.requireNonNull(method);
        this.path = Objects.requireNonNull(path);
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
        this.requestBody = requestBody;
        this.responseType = responseType;
        this.deprecated = deprecated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
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

    public TypeDescriptor getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(TypeDescriptor requestBody) {
        this.requestBody = requestBody;
    }

    public TypeDescriptor getResponseType() {
        return responseType;
    }

    public void setResponseType(TypeDescriptor responseType) {
        this.responseType = responseType;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
}
