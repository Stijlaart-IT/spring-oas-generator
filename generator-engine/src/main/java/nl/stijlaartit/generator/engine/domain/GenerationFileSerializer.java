package nl.stijlaartit.generator.engine.domain;

public interface GenerationFileSerializer<T extends GenerationFile> {

    SerializedFile serialize(T file);

    boolean supports(GenerationFile generationFile);
}
