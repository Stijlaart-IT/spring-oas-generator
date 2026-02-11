package nl.stijlaartit.generator.domain;

import java.util.List;

public interface ModelFile extends GenerationFile {
    List<String> getDependencies();
}
