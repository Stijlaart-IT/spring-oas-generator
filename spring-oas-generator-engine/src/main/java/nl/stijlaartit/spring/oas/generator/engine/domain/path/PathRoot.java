package nl.stijlaartit.spring.oas.generator.engine.domain.path;

import nl.stijlaartit.spring.oas.generator.engine.domain.OperationName;

import java.util.Objects;

public sealed interface PathRoot permits PathRoot.ComponentParameter, PathRoot.ComponentSchema, PathRoot.RequestBody, PathRoot.RequestParam, PathRoot.ResponseBody, PathRoot.SharedPathParam {

    public static PathRoot componentSchema(String name) {
        return new ComponentSchema(name);
    }

    static PathRoot requestParam(OperationName operationName, String paramName) {
        return new RequestParam(operationName, paramName);
    }

    static PathRoot sharedPathParam(String path, String paramName) {
        return new SharedPathParam(path, paramName);
    }

    static PathRoot componentParameter(String name) {
        return new ComponentParameter(name);
    }
    static PathRoot requestBody(OperationName operationName) {
        return new RequestBody(operationName);
    }

    static PathRoot responseBody(OperationName operationName, String status) {
        return new ResponseBody(operationName, status);
    }

    record SharedPathParam(String path, String name) implements PathRoot {
        public SharedPathParam {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(name, "name");
        }
    }
    record ComponentSchema(String name) implements PathRoot, NamedPathRoot {
        public ComponentSchema {
            Objects.requireNonNull(name, "name");
        }
    }
    record ComponentParameter(String name) implements PathRoot, NamedPathRoot {
        public ComponentParameter {
            Objects.requireNonNull(name, "name");
        }
    }

    record RequestBody(OperationName operationName) implements PathRoot {
        public RequestBody {
            Objects.requireNonNull(operationName, "operationName");
        }
    }
    record ResponseBody(OperationName operationName, String status) implements PathRoot {
        public ResponseBody {
            Objects.requireNonNull(operationName, "operationName");
            Objects.requireNonNull(status, "status");
        }
    }

    record RequestParam(OperationName operationName, String paramName) implements PathRoot {
        public RequestParam {
            Objects.requireNonNull(operationName, "operationName");
            Objects.requireNonNull(paramName, "paramName");
        }
    }
}
