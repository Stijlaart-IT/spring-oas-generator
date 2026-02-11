package nl.stijlaartit.generator.domain;

import java.util.List;

public interface ModelFile {
    String getName();

    void setName(String name);

    List<String> getDependencies();
}
