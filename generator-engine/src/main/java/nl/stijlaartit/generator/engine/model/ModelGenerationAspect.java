package nl.stijlaartit.generator.engine.model;

import nl.stijlaartit.generator.engine.domain.GenerationAspect;
import nl.stijlaartit.generator.engine.domain.GenerationFile;
import nl.stijlaartit.generator.engine.domain.GenerationFileWriter;
import nl.stijlaartit.generator.engine.domain.ModelFile;
import nl.stijlaartit.generator.engine.domain.Resolver;

import java.util.Optional;
import java.util.Set;

public class ModelGenerationAspect implements GenerationAspect {
    private final ModelResolver resolver = new ModelResolver();
    private final ModelWriter writer;

    public ModelGenerationAspect(String modelsPackage) {
        this.writer = new ModelWriter(modelsPackage);
    }

    @Override
    public String getId() {
        return "model";
    }

    @Override
    public Set<String> getDependencies() {
        return Set.of();
    }

    @Override
    public Resolver<?> getResolver() {
        return resolver;
    }

    @Override
    public Optional<GenerationFileWriter<? extends GenerationFile>> getWriter() {
        return Optional.of(writer);
    }

    @Override
    public Class<? extends GenerationFile> getFileType() {
        return ModelFile.class;
    }
}
