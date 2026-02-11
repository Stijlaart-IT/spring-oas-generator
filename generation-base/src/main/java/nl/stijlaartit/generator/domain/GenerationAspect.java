package nl.stijlaartit.generator.domain;

import java.util.Optional;
import java.util.Set;

public interface GenerationAspect {
    String getId();

    Set<String> getDependencies();

    Resolver<?> getResolver();

    Optional<GenerationFileWriter<? extends GenerationFile>> getWriter();

    Class<? extends GenerationFile> getFileType();
}
