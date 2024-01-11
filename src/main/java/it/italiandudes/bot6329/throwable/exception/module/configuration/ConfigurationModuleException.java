package it.italiandudes.bot6329.throwable.exception.module.configuration;

import it.italiandudes.bot6329.throwable.exception.ModuleException;

@SuppressWarnings("unused")
public class ConfigurationModuleException extends ModuleException {
    public ConfigurationModuleException(String message) {
        super(message);
    }
    public ConfigurationModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
