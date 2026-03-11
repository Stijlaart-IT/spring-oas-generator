package nl.stijlaartit.spring.oas.generator.domain.file;

import java.util.List;
import java.util.Objects;

public record SpringConfigFile(
        String packageName,
        String apiPackage,
        String serviceGroupName,
        List<String> apiTypeNames
) implements GenerationFile {

    public SpringConfigFile {
        Objects.requireNonNull(packageName, "packageName");
        Objects.requireNonNull(apiPackage, "apiPackage");
        Objects.requireNonNull(serviceGroupName, "serviceGroupName");
        Objects.requireNonNull(apiTypeNames, "apiTypeNames");
    }

    @Override
    public String name() {
        return "ApiConfiguration";
    }
}
