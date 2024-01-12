package it.italiandudes.bot6329.throwables.exceptions.module.generic;

import it.italiandudes.bot6329.throwables.exceptions.ModuleException;

@SuppressWarnings("unused")
public class ModuleUnloadingException extends ModuleException {
    public ModuleUnloadingException(String message) {
        super(message);
    }
    public ModuleUnloadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
