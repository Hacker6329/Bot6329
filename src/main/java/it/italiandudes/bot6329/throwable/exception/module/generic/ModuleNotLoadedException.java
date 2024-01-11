package it.italiandudes.bot6329.throwable.exception.module.generic;

import it.italiandudes.bot6329.throwable.exception.ModuleException;

@SuppressWarnings("unused")
public class ModuleNotLoadedException extends ModuleException {
    public ModuleNotLoadedException(String message) {
        super(message);
    }
    public ModuleNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}