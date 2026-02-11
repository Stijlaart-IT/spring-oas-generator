package nl.stijlaartit.generator.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenerationContext {
    private final List<GenerationFile> files = new ArrayList<>();

    public List<GenerationFile> getFiles() {
        return files;
    }

    public <T extends GenerationFile> List<T> getFiles(Class<T> type) {
        Objects.requireNonNull(type);
        return files.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    public void addFile(GenerationFile file) {
        files.add(Objects.requireNonNull(file));
    }

    public void addFiles(List<? extends GenerationFile> newFiles) {
        Objects.requireNonNull(newFiles);
        newFiles.forEach(this::addFile);
    }
}
