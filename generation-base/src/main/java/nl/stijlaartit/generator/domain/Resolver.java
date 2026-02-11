package nl.stijlaartit.generator.domain;

public interface Resolver<T> {
    void resolve(T input, GenerationContext context);
}
