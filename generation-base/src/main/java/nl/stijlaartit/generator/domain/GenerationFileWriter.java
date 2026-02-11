package nl.stijlaartit.generator.domain;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface GenerationFileWriter<T extends GenerationFile> {
    void writeAll(List<T> files, Path outputDirectory) throws IOException;
}
