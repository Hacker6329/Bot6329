package it.italiandudes.bot6329.exception.module.generic;

import it.italiandudes.bot6329.exception.ModuleException;

@SuppressWarnings("unused")
public class ModuleUnloadingException extends ModuleException {
    public ModuleUnloadingException(String message) {
        super(message);
    }
    public ModuleUnloadingException(String message, Throwable cause) {
        super(message, cause);
    }
}