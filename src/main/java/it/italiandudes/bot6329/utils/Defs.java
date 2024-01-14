package it.italiandudes.bot6329.utils;

import it.italiandudes.bot6329.Bot6329;

import java.io.File;
import java.net.URISyntaxException;

public final class Defs {

    // Jar Position
    public static final String JAR_PATH;
    public static final String JAR_DIRECTORY_PATH;
    static {
        try {
            File jarPathFile = new File(Bot6329.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            JAR_PATH = jarPathFile.getAbsolutePath();
            JAR_DIRECTORY_PATH = jarPathFile.getParent() + File.separator;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
