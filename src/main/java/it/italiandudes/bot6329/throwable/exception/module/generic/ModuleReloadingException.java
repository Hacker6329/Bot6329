package it.italiandudes.bot6329.throwable.exception.module.generic;

import it.italiandudes.bot6329.throwable.exception.ModuleException;

@SuppressWarnings("unused")
public final class ModuleReloadingException extends ModuleException {
    public ModuleReloadingException(String message) {
        super(message);
    }
    public ModuleReloadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
