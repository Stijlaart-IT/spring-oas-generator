package nl.stijlaartit.generation.client;

import nl.stijlaartit.generator.domain.GenerationAspect;
import nl.stijlaartit.generator.domain.GenerationFile;
import nl.stijlaartit.generator.domain.GenerationFileWriter;
import nl.stijlaartit.generator.domain.ApiFile;
import nl.stijlaartit.generator.domain.Resolver;

import java.util.Optional;
import java.util.Set;

public class ClientGenerationAspect implements GenerationAspect {
    private final ClientResolver resolver = new ClientResolver();
    private final ClientWriter writer;

    public ClientGenerationAspect(String clientPackage, String modelsPackage) {
        this.writer = new ClientWriter(clientPackage, modelsPackage);
    }

    @Override
    public String getId() {
        return "client";
    }

    @Override
    public Set<String> getDependencies() {
        return Set.of("model");
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
        return ApiFile.class;
    }
}
