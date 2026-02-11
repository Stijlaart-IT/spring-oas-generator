package nl.stijlaartit.generator.domain;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WriteReport {
    private final Map<Path, Integer> countsByDirectory = new LinkedHashMap<>();
    private final List<Path> files = new ArrayList<>();

    public void recordFile(Path filePath) {
        Objects.requireNonNull(filePath);
        files.add(filePath);
        Path directory = Objects.requireNonNull(filePath.getParent(),
                "File path must have a parent directory: " + filePath);
        countsByDirectory.merge(directory, 1, Integer::sum);
    }

    public int getTotalFiles() {
        return files.size();
    }

    public Map<Path, Integer> getCountsByDirectory() {
        return Collections.unmodifiableMap(countsByDirectory);
    }

    public List<Path> getFiles() {
        return Collections.unmodifiableList(files);
    }
}
