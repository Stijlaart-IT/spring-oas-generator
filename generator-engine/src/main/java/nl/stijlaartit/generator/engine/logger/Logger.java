package nl.stijlaartit.generator.engine.logger;

public interface Logger {

    void debug(String message);

    void debug(String message, Throwable error);

    void info(String message);

    void info(String message, Throwable error);

    void warn(String message);

    void warn(String message, Throwable error);

    void error(String message);

    void error(String message, Throwable error);


    static Logger noOp() {
        return new Logger() {
            @Override
            public void debug(String message) {
            }

            @Override
            public void debug(String message, Throwable error) {
            }

            @Override
            public void info(String message) {
            }

            @Override
            public void info(String message, Throwable error) {
            }

            @Override
            public void warn(String message) {
            }

            @Override
            public void warn(String message, Throwable error) {
            }

            @Override
            public void error(String message) {
            }

            @Override
            public void error(String message, Throwable error) {
            }
        };
    }
}
