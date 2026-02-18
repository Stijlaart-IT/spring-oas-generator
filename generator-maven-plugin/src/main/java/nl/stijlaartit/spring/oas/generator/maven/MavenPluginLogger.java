package nl.stijlaartit.spring.oas.generator.maven;

import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;
import org.apache.maven.plugin.logging.Log;

public class MavenPluginLogger implements Logger {
    private final Log delegate;

    public MavenPluginLogger(Log log) {
        this.delegate = log;
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
        delegate.info(message);
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
