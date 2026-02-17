package nl.stijlaartit.generator.engine.domain;

import java.util.Objects;

public record PackageInfoFile(String packageName, PackageInfoType type) implements GenerationFile {

    public PackageInfoFile {
        Objects.requireNonNull(packageName, "packageName");
        Objects.requireNonNull(type, "type");
    }

    public static PackageInfoFile model(String packageName) {
        return new PackageInfoFile(packageName, PackageInfoType.MODEL);
    }

    public static PackageInfoFile api(String packageName) {
        return new PackageInfoFile(packageName, PackageInfoType.API);
    }

    @Override
    public String name() {
        return "package-info";
    }
}
