package nl.stijlaartit.spring.oas.generator.engine.utility;

import nl.stijlaartit.spring.oas.generator.domain.file.ApiFile;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordField;
import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;
import nl.stijlaartit.spring.oas.generator.domain.file.ModelFile;
import nl.stijlaartit.spring.oas.generator.domain.file.NullWrapperFile;
import nl.stijlaartit.spring.oas.generator.domain.file.PackageInfoFile;
import nl.stijlaartit.spring.oas.generator.domain.file.RecordModel;
import nl.stijlaartit.spring.oas.generator.domain.file.SpringConfigFile;
import nl.stijlaartit.spring.oas.generator.engine.SpringConfigGenerationConfig;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UtilityResolver {
    private final String modelsPackage;
    private final String clientPackage;
    private final String configPackage;
    private final @Nullable SpringConfigGenerationConfig springConfigGenerationConfig;

    public UtilityResolver(String modelsPackage, String clientPackage, String configPackage, @Nullable SpringConfigGenerationConfig springConfigGenerationConfig) {
        this.modelsPackage = Objects.requireNonNull(modelsPackage, "modelsPackage");
        this.clientPackage = Objects.requireNonNull(clientPackage, "clientPackage");
        this.configPackage = Objects.requireNonNull(configPackage, "configPackage");
        this.springConfigGenerationConfig = springConfigGenerationConfig;
    }

    public List<GenerationFile> resolve(List<ModelFile> models, List<ApiFile> clients) {
        List<GenerationFile> files = new ArrayList<>();
        if (!models.isEmpty()) {
            files.add(PackageInfoFile.model(modelsPackage));
        }
        if (!clients.isEmpty()) {
            files.add(PackageInfoFile.api(clientPackage));
        }
        if (requiresNullWrapper(models)) {
            files.add(new NullWrapperFile(modelsPackage));
        }
        if (springConfigGenerationConfig != null && !clients.isEmpty()) {
            files.add(new SpringConfigFile(
                    configPackage,
                    clientPackage,
                    springConfigGenerationConfig.serviceGroupName(),
                    clients.stream().map(ApiFile::name).toList()
            ));
        }
        return files;
    }

    private static boolean requiresNullWrapper(List<ModelFile> models) {
        for (ModelFile model : models) {
            if (model instanceof RecordModel recordModel) {
                for (RecordField field : recordModel.fields()) {
                    if (!field.required() && field.nullable()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
