package nl.stijlaartit.generator.engine.domain;

public interface Resolver<T> {
    void resolve(T input, GenerationContext context);
}
