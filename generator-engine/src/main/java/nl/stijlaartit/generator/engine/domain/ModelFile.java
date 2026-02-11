package nl.stijlaartit.generator.engine.domain;

import java.util.List;

public interface ModelFile extends GenerationFile {
    List<String> getDependencies();
}
