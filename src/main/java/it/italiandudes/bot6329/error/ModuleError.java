package it.italiandudes.bot6329.error;

@SuppressWarnings("unused")
public class ModuleError extends Error {
    public ModuleError(String message) {
        super(message);
    }
    public ModuleError(String message, Throwable cause) {
        super(message, cause);
    }
}
