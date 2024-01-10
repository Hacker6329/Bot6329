package it.italiandudes.bot6329.exception.module.generic;

import it.italiandudes.bot6329.exception.ModuleException;

@SuppressWarnings("unused")
public class ModuleAlreadyLoadedException extends ModuleException {
    public ModuleAlreadyLoadedException(String message) {
        super(message);
    }
    public ModuleAlreadyLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
