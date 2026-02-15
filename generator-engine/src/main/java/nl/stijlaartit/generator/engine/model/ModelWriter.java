package nl.stijlaartit.generator.engine.model;

import com.palantir.javapoet.JavaFile;
import nl.stijlaartit.generator.engine.domain.EnumModel;
import nl.stijlaartit.generator.engine.domain.GenerationFileWriter;
import nl.stijlaartit.generator.engine.domain.ModelFile;
import nl.stijlaartit.generator.engine.domain.RecordModel;
import nl.stijlaartit.generator.engine.domain.UnionModelFile;
import nl.stijlaartit.generator.engine.domain.OneOfVariant;
import nl.stijlaartit.generator.engine.domain.WriteReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ModelWriter implements GenerationFileWriter<ModelFile> {
    private final String modelsPackage;
    private final RecordModelWriter recordModelWriter;
    private final EnumModelWriter enumModelWriter;
    private final UnionModelWriter unionModelWriter;

    public ModelWriter(String modelsPackage, RecordModelWriterConfig recordModelWriterConfig) {
        this.modelsPackage = modelsPackage;
        this.recordModelWriter = new RecordModelWriter(modelsPackage, recordModelWriterConfig);
        this.enumModelWriter = new EnumModelWriter(modelsPackage);
        this.unionModelWriter = new UnionModelWriter(modelsPackage);
    }

    @Override
    public WriteReport writeAll(List<ModelFile> models, Path outputDirectory) throws IOException {
        WriteReport report = new WriteReport();
        report.recordFile(writePackageInfo(outputDirectory));
        Map<String, List<String>> implementsByModel = resolveImplementsByModel(models);
        for (ModelFile model : models) {
            write(model, outputDirectory, implementsByModel);
            report.recordFile(modelPath(outputDirectory, model.name()));
        }
        return report;
    }

    public void write(ModelFile model, Path outputDirectory, Map<String, List<String>> implementsByModel) throws IOException {
        toJavaFile(model, implementsByModel).writeTo(outputDirectory);
    }

    JavaFile toJavaFile(ModelFile model, Map<String, List<String>> implementsByModel) {
        return switch (model) {
            case EnumModel enumDescriptor -> enumModelWriter.toJavaFile(enumDescriptor, implementsByModel);
            case RecordModel recordDescriptor -> recordModelWriter.toJavaFile(recordDescriptor, implementsByModel);
            case UnionModelFile oneOfDescriptor -> unionModelWriter.toJavaFile(oneOfDescriptor);
            default -> throw new IllegalArgumentException("Unsupported model schema: " + model.getClass());
        };
    }

    private Path writePackageInfo(Path outputDirectory) throws IOException {
        Path packageDir = outputDirectory.resolve(modelsPackage.replace('.', '/'));
        Files.createDirectories(packageDir);
        Path packageInfo = packageDir.resolve("package-info.java");
        Files.writeString(packageInfo, packageInfoSource(modelsPackage));
        return packageInfo;
    }

    private Path modelPath(Path outputDirectory, String modelName) {
        return outputDirectory.resolve(modelsPackage.replace('.', '/'))
                .resolve(modelName + ".java");
    }

    private static String packageInfoSource(String packageName) {
        return "@NullMarked\n"
                + "package " + packageName + ";\n\n"
                + "import org.jspecify.annotations.NullMarked;\n";
    }

    private static Map<String, List<String>> resolveImplementsByModel(List<ModelFile> models) {
        Map<String, List<String>> implementsByModel = new HashMap<>();
        for (ModelFile model : models) {
            if (!(model instanceof UnionModelFile unionModel)) {
                continue;
            }
            for (OneOfVariant variant : unionModel.variants()) {
                implementsByModel
                        .computeIfAbsent(variant.modelName(), key -> new ArrayList<>())
                        .add(unionModel.name());
            }
        }
        for (Map.Entry<String, List<String>> entry : implementsByModel.entrySet()) {
            List<String> distinct = new ArrayList<>(new LinkedHashSet<>(entry.getValue()));
            entry.setValue(distinct);
        }
        return implementsByModel;
    }
}
