package nl.stijlaartit.generator.engine.utility;

import nl.stijlaartit.generator.engine.domain.ApiFile;
import nl.stijlaartit.generator.engine.domain.FieldModel;
import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.ModelFile;
import nl.stijlaartit.generator.engine.domain.NullWrapperFile;
import nl.stijlaartit.generator.engine.domain.PackageInfoFile;
import nl.stijlaartit.generator.engine.domain.RecordModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UtilityResolver {
    private final String modelsPackage;
    private final String clientPackage;

    public UtilityResolver(String modelsPackage, String clientPackage) {
        this.modelsPackage = Objects.requireNonNull(modelsPackage, "modelsPackage");
        this.clientPackage = Objects.requireNonNull(clientPackage, "clientPackage");
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
        return files;
    }

    private static boolean requiresNullWrapper(List<ModelFile> models) {
        for (ModelFile model : models) {
            if (model instanceof RecordModel recordModel) {
                for (FieldModel field : recordModel.fields()) {
                    if (!field.required() && field.nullable()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
