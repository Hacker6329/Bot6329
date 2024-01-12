package it.italiandudes.bot6329.modules.localization;

import it.italiandudes.bot6329.util.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Localization {
    EN_US,
    IT_IT,
    ;

    // Localization Files Extension
    public static final String LOCALIZATION_FILE_EXTENSION = "json";

    // Fallback Localization
    public static final Localization FALLBACK = EN_US;

    // Methods
    @NotNull
    public static String getLangFilepath(@Nullable final Localization localization) {
        if (localization == null) return getFallbackFilepath();
        return Resource.Localization.LOCALIZATION_DIR + localization.name().toLowerCase() + '.' + LOCALIZATION_FILE_EXTENSION;
    }
    @NotNull
    public static String getFallbackFilepath() {
        return Resource.Localization.LOCALIZATION_DIR + FALLBACK.name().toLowerCase() + '.' + LOCALIZATION_FILE_EXTENSION;
    }
}
