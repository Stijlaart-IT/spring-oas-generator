package nl.stijlaartit.spring.oas.generator.engine.client.raw;

public sealed interface RawOperation permits GeneratableOperation, NonGeneratableOperation {
}
