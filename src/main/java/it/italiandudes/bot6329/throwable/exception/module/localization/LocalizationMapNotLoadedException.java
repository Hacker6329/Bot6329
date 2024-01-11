package it.italiandudes.bot6329.throwable.exception.module.localization;

@SuppressWarnings("unused")
public final class LocalizationMapNotLoadedException extends LocalizationModuleException {
    public LocalizationMapNotLoadedException(String message) {
        super(message);
    }
    public LocalizationMapNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
