package nl.stijlaartit.spring.oas.generator.engine.naming;

import io.swagger.v3.oas.models.PathItem;
import nl.stijlaartit.spring.oas.generator.engine.domain.HttpMethod;

public class OperationIdNaming {


    public static String fallbackOperationId(PathItem.HttpMethod method, String path) {
        return fallbackOperationId(method != null ? method.name() : null, path);
    }

    public static String fallbackOperationId(HttpMethod method, String path) {
        return fallbackOperationId(method != null ? method.name() : null, path);
    }

    private static String fallbackOperationId(String method, String path) {
        String normalized = path == null ? "" : path;
        normalized = normalized.replace("{", "").replace("}", "");
        normalized = normalized.replaceAll("[^A-Za-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        String methodPrefix = method != null ? method.toLowerCase() : "operation";
        if (normalized.isBlank()) {
            return methodPrefix;
        }
        return methodPrefix + "_" + normalized;
    }

}
