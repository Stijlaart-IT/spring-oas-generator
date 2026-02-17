package nl.stijlaartit.generator.engine.utility;

import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.GenerationFileWriter;
import nl.stijlaartit.generator.engine.domain.NullWrapperFile;
import nl.stijlaartit.generator.engine.domain.PackageInfoFile;
import nl.stijlaartit.generator.engine.domain.WriteReport;
import nl.stijlaartit.generator.engine.model.NullWrapperWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class UtilityWriter implements GenerationFileWriter<GenerationFile> {
    private final PackageInfoWriter packageInfoWriter = new PackageInfoWriter();

    @Override
    public WriteReport writeAll(List<GenerationFile> files, Path outputDirectory) throws IOException {
        WriteReport report = new WriteReport();
        for (GenerationFile file : files) {
            if (file instanceof PackageInfoFile packageInfoFile) {
                report.recordFile(packageInfoWriter.write(packageInfoFile, outputDirectory));
                continue;
            }
            if (file instanceof NullWrapperFile nullWrapperFile) {
                report.recordFile(writeNullWrapper(nullWrapperFile, outputDirectory));
                continue;
            }
            throw new IllegalArgumentException("Unsupported utility file: " + file.getClass());
        }
        return report;
    }

    private Path writeNullWrapper(NullWrapperFile file, Path outputDirectory) throws IOException {
        new NullWrapperWriter(file.packageName()).write(outputDirectory);
        return outputDirectory.resolve(file.packageName().replace('.', '/'))
                .resolve("NullWrapper.java");
    }
}
