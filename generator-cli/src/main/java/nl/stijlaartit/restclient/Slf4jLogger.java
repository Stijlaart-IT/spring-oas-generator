package nl.stijlaartit.restclient;

import nl.stijlaartit.generator.engine.logger.Logger;

public class Slf4jLogger implements Logger {
    private final org.slf4j.Logger delegate;

    public Slf4jLogger(org.slf4j.Logger delegate) {
        this.delegate = delegate;
    }

    @Override
    public void debug(String message) {
        delegate.debug(message);
    }

    @Override
    public void debug(String message, Throwable error) {
        delegate.debug(message, error);
    }

    @Override
    public void info(String message) {
        delegate.info(message);
    }

    @Override
    public void info(String message, Throwable error) {
        delegate.info(message, error);
    }

    @Override
    public void warn(String message) {
        delegate.warn(message);
    }

    @Override
    public void warn(String message, Throwable error) {
        delegate.warn(message, error);
    }

    @Override
    public void error(String message) {
        delegate.error(message);
    }

    @Override
    public void error(String message, Throwable error) {
        delegate.error(message, error);
    }
}
