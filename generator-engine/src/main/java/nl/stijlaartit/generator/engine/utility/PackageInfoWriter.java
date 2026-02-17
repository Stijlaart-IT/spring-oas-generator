package nl.stijlaartit.generator.engine.utility;

import nl.stijlaartit.generator.engine.GeneratedAnnotation;
import nl.stijlaartit.generator.engine.domain.PackageInfoFile;
import nl.stijlaartit.generator.engine.domain.PackageInfoType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PackageInfoWriter {

    public Path write(PackageInfoFile file, Path outputDirectory) throws IOException {
        Path packageDir = outputDirectory.resolve(file.packageName().replace('.', '/'));
        Files.createDirectories(packageDir);
        Path packageInfo = packageDir.resolve("package-info.java");
        Files.writeString(packageInfo, packageInfoSource(file));
        return packageInfo;
    }

    private static String packageInfoSource(PackageInfoFile file) {
        if (file.type() == PackageInfoType.API) {
            return GeneratedAnnotation.sourceLine() + "\n"
                    + "@NullMarked\n"
                    + "package " + file.packageName() + ";\n\n"
                    + "import javax.annotation.processing.Generated;\n"
                    + "import org.jspecify.annotations.NullMarked;\n";
        }
        return "@NullMarked\n"
                + "package " + file.packageName() + ";\n\n"
                + "import org.jspecify.annotations.NullMarked;\n";
    }
}
