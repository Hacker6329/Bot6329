package it.italiandudes.bot6329.util;

import it.italiandudes.bot6329.Bot6329;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class Resource {

    //Resource Getters
    public static URL get(@NotNull final String resourceConst) {
        return Objects.requireNonNull(Bot6329.class.getResource(resourceConst));
    }
    public static InputStream getAsStream(@NotNull final String resourceConst) {
        return Objects.requireNonNull(Bot6329.class.getResourceAsStream(resourceConst));
    }

    // Jar Root Directory
    public static final String JAR_ROOT_DIRECTORY = "/";
    public static final class Localization {
        public static final String LOCALIZATION_DIR = JAR_ROOT_DIRECTORY + "localization/";
    }
}
