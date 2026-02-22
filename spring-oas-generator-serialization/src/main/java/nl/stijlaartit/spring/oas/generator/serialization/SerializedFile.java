package nl.stijlaartit.spring.oas.generator.serialization;

import com.palantir.javapoet.JavaFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public sealed interface SerializedFile permits SerializedFile.Ast, SerializedFile.ContentString {

    Path writeTo(Path output) throws IOException;

    record Ast(String packageName, JavaFile javaFile) implements SerializedFile {

        @Override
        public Path writeTo(Path output) throws IOException {
            return javaFile.writeToPath(output);
        }
    }

    record ContentString(String packageName, String fileName, String contents) implements SerializedFile {

        @Override
        public Path writeTo(Path output) throws IOException {
            Path packageDir = output.resolve(packageName.replace('.', '/'));
            Files.createDirectories(packageDir);
            Path packageInfo = packageDir.resolve(fileName);
            Files.writeString(packageInfo, contents);
            return packageInfo;
        }
    }


}
