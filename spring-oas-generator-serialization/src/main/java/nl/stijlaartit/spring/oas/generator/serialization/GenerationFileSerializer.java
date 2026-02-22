package nl.stijlaartit.spring.oas.generator.serialization;

import nl.stijlaartit.spring.oas.generator.domain.file.GenerationFile;

public interface GenerationFileSerializer<T extends GenerationFile> {

    SerializedFile serialize(T file);

    boolean supports(GenerationFile generationFile);
}
