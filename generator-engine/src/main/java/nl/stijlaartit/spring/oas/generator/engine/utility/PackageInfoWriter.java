package nl.stijlaartit.spring.oas.generator.engine.utility;

import nl.stijlaartit.spring.oas.generator.engine.GeneratedAnnotation;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.GenerationFileSerializer;
import nl.stijlaartit.spring.oas.generator.engine.domain.PackageInfoFile;
import nl.stijlaartit.spring.oas.generator.engine.domain.PackageInfoType;
import nl.stijlaartit.spring.oas.generator.engine.domain.SerializedFile;
import org.jspecify.annotations.NonNull;

public class PackageInfoWriter implements GenerationFileSerializer<PackageInfoFile> {

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
