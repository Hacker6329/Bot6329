package it.italiandudes.bot6329.exception;

@SuppressWarnings("unused")
public class ModuleException extends Exception {
    public ModuleException(String message) {
        super(message);
    }
    public ModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
