package nl.stijlaartit.spring.oas.generator.serialization;

import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;
import nl.stijlaartit.spring.oas.generator.domain.file.PackageInfoFile;
import nl.stijlaartit.spring.oas.generator.domain.file.PackageInfoType;
import org.jspecify.annotations.NonNull;

public class PackageInfoSerializer implements GenerationFileSerializer<PackageInfoFile> {

    @Override
    @NonNull
    public SerializedFile serialize(PackageInfoFile file) {
        return new SerializedFile.ContentString(file.packageName(), "package-info.java", packageInfoSource(file));
    }

    @Override
    public boolean supports(@NonNull GenerationFile generationFile) {
        return generationFile instanceof PackageInfoFile;
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
