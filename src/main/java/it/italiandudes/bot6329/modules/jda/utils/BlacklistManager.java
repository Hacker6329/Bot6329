package it.italiandudes.bot6329.modules.jda.utils;

import it.italiandudes.bot6329.modules.database.ModuleDatabase;
import it.italiandudes.bot6329.modules.database.entries.DatabaseGuildSettings;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public final class BlacklistManager {

    // Blacklists
    private static HashSet<@NotNull String> NO_BLACKLIST_GUILDS = null;
    private static HashMap<@NotNull String, @NotNull HashSet<@NotNull String>> GUILDS_BLACKLIST = null;

    // Methods
    public static void initBlacklist() throws SQLException {
        String query = "SELECT guild_id FROM guild_settings WHERE setting_key=?;";
        PreparedStatement ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, DatabaseGuildSettings.KEY_DISABLE_BLACKLIST);
        ResultSet result = ps.executeQuery();
        NO_BLACKLIST_GUILDS = new HashSet<>();
        while (result.next()) {
            NO_BLACKLIST_GUILDS.add(result.getString("guild_id"));
        }
        ps.close();
        query = "SELECT * FROM guilds_blacklist;";
        ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        result = ps.executeQuery();
        GUILDS_BLACKLIST = new HashMap<>();
        while (result.next()) {
            if (!GUILDS_BLACKLIST.containsKey(result.getString("guild_id"))) GUILDS_BLACKLIST.put(result.getString("guild_id"), new HashSet<>());
            GUILDS_BLACKLIST.get(result.getString("guild_id")).add(result.getString("user_id"));
        }
        ps.close();
    }
    public static boolean disableGuildBlacklist(@NotNull final String guildID) throws SQLException {
        if (NO_BLACKLIST_GUILDS.contains(guildID)) return false;
        ModuleJDA.getInstance().writeGuildSetting(guildID, DatabaseGuildSettings.KEY_DISABLE_BLACKLIST, "1");
        return NO_BLACKLIST_GUILDS.add(guildID);
    }
    public static boolean enableGuildBlacklist(@NotNull final String guildID) throws SQLException {
        if (!NO_BLACKLIST_GUILDS.contains(guildID)) return false;
        ModuleJDA.getInstance().writeGuildSetting(guildID, DatabaseGuildSettings.KEY_DISABLE_BLACKLIST, "0");
        return NO_BLACKLIST_GUILDS.remove(guildID);
    }
    public static boolean addUserToGuildBlacklist(@NotNull final String guildID, @NotNull final String userID) throws SQLException {
        if (!GUILDS_BLACKLIST.containsKey(guildID)) GUILDS_BLACKLIST.put(guildID, new HashSet<>());
        if (GUILDS_BLACKLIST.get(guildID).contains(userID)) return false;
        String query = "INSERT INTO guilds_blacklist (guild_id, user_id) VALUES (?, ?);";
        PreparedStatement ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, guildID);
        ps.setString(2, userID);
        ps.executeUpdate();
        ps.close();
        return GUILDS_BLACKLIST.get(guildID).add(userID);
    }
    public static boolean removeUserFromGuildBlacklist(@NotNull final String guildID, @NotNull final String userID) throws SQLException {
        if (!GUILDS_BLACKLIST.containsKey(guildID)) GUILDS_BLACKLIST.put(guildID, new HashSet<>());
        if (!GUILDS_BLACKLIST.get(guildID).contains(userID)) return false;
        String query = "REMOVE FROM guilds_blacklist WHERE guild_id=? AND user_id=?;";
        PreparedStatement ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, guildID);
        ps.setString(2, userID);
        ps.executeUpdate();
        ps.close();
        return GUILDS_BLACKLIST.get(guildID).remove(userID);
    }
    public static boolean isUserBlacklisted(@NotNull final String guildID, @NotNull final String userID) {
        if (NO_BLACKLIST_GUILDS.contains(guildID)) return false;
        if (!GUILDS_BLACKLIST.containsKey(guildID)) GUILDS_BLACKLIST.put(guildID, new HashSet<>());
        return GUILDS_BLACKLIST.get(guildID).contains(userID);
    }
}
