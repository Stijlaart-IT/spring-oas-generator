package nl.stijlaartit.generation.model;

import nl.stijlaartit.generator.domain.GenerationAspect;
import nl.stijlaartit.generator.domain.GenerationFile;
import nl.stijlaartit.generator.domain.GenerationFileWriter;
import nl.stijlaartit.generator.domain.ModelFile;
import nl.stijlaartit.generator.domain.Resolver;

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
    public GenerationFileWriter<? extends GenerationFile> getWriter() {
        return writer;
    }

    @Override
    public Class<? extends GenerationFile> getFileType() {
        return ModelFile.class;
    }
}
