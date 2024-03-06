package it.italiandudes.bot6329.modules.jda.utils;

import it.italiandudes.bot6329.modules.database.ModuleDatabase;
import it.italiandudes.bot6329.modules.database.entries.DatabaseGuildSettings;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public final class GreetingsManager {

    // Blacklists
    private static HashSet<@NotNull String> NO_GREETINGS_GUILDS = null;
    private static HashMap<@NotNull String, @NotNull String> GREETINGS_GUILD_CHANNELS = null;
    private static HashMap<@NotNull String, @NotNull String> GUILDS_GREETINGS_MESSAGES = null;

    // Methods
    public static void initWelcomeManager() throws SQLException {
        String query = "SELECT * FROM guild_settings WHERE setting_key=?;";
        PreparedStatement ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, DatabaseGuildSettings.KEY_GREETINGS_MESSAGE);
        ResultSet result = ps.executeQuery();
        GUILDS_GREETINGS_MESSAGES = new HashMap<>();
        while (result.next()) {
            GUILDS_GREETINGS_MESSAGES.put(result.getString("guild_id"), result.getString("setting_value"));
        }
        ps.close();
        query = "SELECT guild_id FROM guild_settings WHERE setting_key=? AND setting_value=?;";
        ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, DatabaseGuildSettings.KEY_DISABLE_GREETINGS);
        ps.setString(2, "1");
        result = ps.executeQuery();
        NO_GREETINGS_GUILDS = new HashSet<>();
        while (result.next()) {
            NO_GREETINGS_GUILDS.add(result.getString("guild_id"));
        }
        ps.close();
        query = "SELECT * FROM guild_settings WHERE setting_key=?;";
        ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, DatabaseGuildSettings.KEY_GREETINGS_CHANNEL);
        result = ps.executeQuery();
        GREETINGS_GUILD_CHANNELS = new HashMap<>();
        while (result.next()) {
            GREETINGS_GUILD_CHANNELS.put(result.getString("guild_id"), result.getString("setting_value"));
        }
        ps.close();
    }
    public static boolean disableGuildGreetings(@NotNull final String guildID) throws SQLException {
        if (NO_GREETINGS_GUILDS.contains(guildID)) return false;
        ModuleJDA.getInstance().writeGuildSetting(guildID, DatabaseGuildSettings.KEY_DISABLE_GREETINGS, "1");
        return NO_GREETINGS_GUILDS.add(guildID);
    }
    public static boolean enableGuildGreetings(@NotNull final String guildID) throws SQLException {
        if (!NO_GREETINGS_GUILDS.contains(guildID)) return false;
        if (!GREETINGS_GUILD_CHANNELS.containsKey(guildID)) return false;
        ModuleJDA.getInstance().writeGuildSetting(guildID, DatabaseGuildSettings.KEY_DISABLE_GREETINGS, "0");
        return NO_GREETINGS_GUILDS.remove(guildID);
    }
    @Nullable
    public static TextChannel getGreetingsChannel(@NotNull final Guild guild) {
        String channelID = GREETINGS_GUILD_CHANNELS.get(guild.getId());
        return channelID!=null?guild.getTextChannelById(channelID):guild.getSystemChannel();
    }
    public static void setGreetingsChannel(@NotNull final Guild guild, @NotNull final TextChannel channel) throws SQLException {
        String oldChannel = GREETINGS_GUILD_CHANNELS.get(guild.getId());
        if (channel.getId().equals(oldChannel)) return;
        GREETINGS_GUILD_CHANNELS.put(guild.getId(), channel.getId());
        ModuleJDA.getInstance().writeGuildSetting(guild.getId(), DatabaseGuildSettings.KEY_GREETINGS_CHANNEL, channel.getId());
    }
    @NotNull
    public static String getGuildGreetingsMessage(@NotNull final String guildID) throws SQLException {
        String message = GREETINGS_GUILD_CHANNELS.get(guildID);
        if (message == null) {
            message = GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_DEFAULT_MESSAGE);
            setGuildGreetingsMessage(guildID, message);
        }
        return message;
    }
    public static void setGuildGreetingsMessage(@NotNull final String guildID, @NotNull final String message) throws SQLException {
        GREETINGS_GUILD_CHANNELS.put(guildID, message);
        ModuleJDA.getInstance().writeGuildSetting(guildID, DatabaseGuildSettings.KEY_GREETINGS_MESSAGE, message);
    }
    @NotNull
    public static String getMessageProperties() {
        return "TODO";
    }

    // Constants
    public static final class GreetingsConstants {
        public static final String USER = "{USER}";
        public static final String USER_ID = "{USERID}";
        public static final String USERNAME = "{USERNAME}";
        public static final String GUILD = "{GUILD}";
        public static final String GUILD_ID = "{GUILDID}";
    }
}
