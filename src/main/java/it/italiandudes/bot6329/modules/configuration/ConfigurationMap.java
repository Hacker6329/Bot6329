package it.italiandudes.bot6329.modules.configuration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

public final class ConfigurationMap {

    // Default Configuration Map
    public static final HashMap<String, Object> DEFAULT_CONFIGURATION = new HashMap<>();
    static {
        DEFAULT_CONFIGURATION.put(Keys.TOKEN, null);
        DEFAULT_CONFIGURATION.put(Keys.DATABASE_PATH, "bot6329.sqlite3");
    }

    public static void fixEntry(@NotNull final JSONObject CONFIGURATION, @NotNull final String KEY) {
        CONFIGURATION.remove(KEY);
        CONFIGURATION.put(KEY, DEFAULT_CONFIGURATION.get(KEY));
    }

    // Keys
    public static final class Keys {
        public static final String TOKEN = "token";
        public static final String DATABASE_PATH = "database_path";
    }
}
