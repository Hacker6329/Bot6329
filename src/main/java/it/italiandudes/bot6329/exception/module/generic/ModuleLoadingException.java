package it.italiandudes.bot6329.exception.module.generic;

import it.italiandudes.bot6329.exception.ModuleException;

@SuppressWarnings("unused")
public class ModuleLoadingException extends ModuleException {
    public ModuleLoadingException(String message) {
        super(message);
    }
    public ModuleLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
