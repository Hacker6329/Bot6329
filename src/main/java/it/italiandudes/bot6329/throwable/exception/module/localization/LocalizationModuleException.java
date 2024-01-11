package it.italiandudes.bot6329.throwable.exception.module.localization;

import it.italiandudes.bot6329.throwable.exception.ModuleException;

@SuppressWarnings("unused")
public class LocalizationModuleException extends ModuleException {
    public LocalizationModuleException(String message) {
        super(message);
    }
    public LocalizationModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
