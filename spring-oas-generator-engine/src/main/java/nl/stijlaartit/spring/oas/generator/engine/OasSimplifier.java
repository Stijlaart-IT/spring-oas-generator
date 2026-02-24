package nl.stijlaartit.spring.oas.generator.engine;

import io.swagger.v3.oas.models.OpenAPI;
import nl.stijlaartit.spring.oas.generator.engine.domain.simplified.SimplifiedOas;
import nl.stijlaartit.spring.oas.generator.engine.logger.Logger;

import static io.swagger.v3.oas.models.SpecVersion.V30;
import static io.swagger.v3.oas.models.SpecVersion.V31;

public class OasSimplifier {

    private final Logger logger;

    public OasSimplifier(Logger logger) {
        this.logger = logger;
    }

    public SimplifiedOas simplify(OpenAPI openAPI) {
        if (openAPI.getSpecVersion().equals(V30)) {
            return new Oas30ToSimpleSchemaMapper(logger).resolve(openAPI);
        } else if (openAPI.getSpecVersion().equals(V31)) {
            return new Oas31ToSimpleSchemaMapper(logger).resolve(openAPI);
        } else {
            logger.error("Unsupported OpenAPI version: " + openAPI.getOpenapi());
            throw new IllegalStateException("Unsupported OpenAPI version: " + openAPI.getOpenapi());
        }
    }
}
