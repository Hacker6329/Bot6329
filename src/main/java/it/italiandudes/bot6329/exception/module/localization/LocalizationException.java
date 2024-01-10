package it.italiandudes.bot6329.exception.module.localization;

@SuppressWarnings("unused")
public class LocalizationException extends RuntimeException {
    public LocalizationException(String message) {
        super(message);
    }
    public LocalizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
