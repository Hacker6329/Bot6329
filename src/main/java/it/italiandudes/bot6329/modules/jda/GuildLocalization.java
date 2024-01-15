package it.italiandudes.bot6329.modules.jda;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.database.entries.DatabaseGuildSettings;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;

public final class GuildLocalization {

    // Attributes
    private static final HashMap<String, Localization> guildLocalizationMap = new HashMap<>();
    // TODO: implement a check for when leaving a guild to check if a localization map is still used by another server, if not unload the localization map

    // Methods
    @NotNull
    public static String localizeString(@NotNull final String guildID, @NotNull final String key) {
        return localizeString(guildID, key, (Object[]) null);
    }
    @NotNull
    public static String localizeString(@NotNull final String guildID, @NotNull final String key, @Nullable Object... param) {
        String localizedMessage = getGuildLocalization(guildID).localizeString(key);
        try {
            return MessageFormat.format(localizedMessage, param);
        } catch (IllegalArgumentException e) {
            return localizedMessage;
        }
    }
    @NotNull
    public static Localization getGuildLocalization(@NotNull final String guildID) {
        if (guildLocalizationMap.containsKey(guildID)) return guildLocalizationMap.get(guildID);
        try {
            if (!ModuleJDA.getInstance().isGuildSettingPresent(guildID, DatabaseGuildSettings.KEY_LOCALIZATION)) {
                updateGuildLocalization(guildID, Localization.FALLBACK);
                return Localization.FALLBACK;
            }
            Localization localization = Localization.getLocalizationByLocale(ModuleJDA.getInstance().readGuildSetting(guildID, DatabaseGuildSettings.KEY_LOCALIZATION));
            if (localization == null) {
                localization = Localization.FALLBACK;
                updateGuildLocalization(guildID, Localization.FALLBACK);
            }
            return localization;
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
            return Localization.FALLBACK;
        }
    }
    public static void updateGuildLocalization(@NotNull final String guildID, @NotNull final Localization localization) {
        try {
            ModuleJDA.getInstance().writeGuildSetting(guildID, DatabaseGuildSettings.KEY_LOCALIZATION, localization.toString());
            guildLocalizationMap.put(guildID, localization);
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }

}
