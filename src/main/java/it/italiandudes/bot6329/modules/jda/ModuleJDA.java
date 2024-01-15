package it.italiandudes.bot6329.modules.jda;

import it.italiandudes.bot6329.modules.BaseModule;
import it.italiandudes.bot6329.modules.ModuleState;
import it.italiandudes.bot6329.modules.configuration.ConfigurationMap;
import it.italiandudes.bot6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.bot6329.modules.database.ModuleDatabase;
import it.italiandudes.bot6329.modules.jda.commands.*;
import it.italiandudes.bot6329.modules.jda.listeners.InactivityListener;
import it.italiandudes.bot6329.modules.jda.listeners.MasterListener;
import it.italiandudes.bot6329.modules.jda.listeners.VoiceChannelListener;
import it.italiandudes.bot6329.throwables.errors.ModuleError;
import it.italiandudes.bot6329.throwables.exceptions.ModuleException;
import it.italiandudes.bot6329.throwables.exceptions.module.configuration.ConfigurationModuleException;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class ModuleJDA extends BaseModule {

    // Attributes
    private JDA jda = null;
    private ArrayList<String> userBlacklist = null;

    // Module Management Methods
    @Override
    protected synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        String token;
        try {
            token = (String) ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.TOKEN);
        } catch (ClassCastException cce) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Error! (Reason: the token in configuration wasn't a string)");
        } catch (ConfigurationModuleException cme) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: the token was not provided in configuration)", cme);
        }

        loadBlacklist();

        try {
            JDABuilder jdaBuilder = JDABuilder.create(token, Arrays.asList(Defs.GATEWAY_INTENTS));
            jdaBuilder.enableCache(Arrays.asList(Defs.ENABLED_CACHE_FLAGS));
            jdaBuilder.disableCache(Arrays.asList(Defs.DISABLED_CACHE_FLAGS));
            jda = jdaBuilder.build().awaitReady();
        } catch (InvalidTokenException invalidTokenException) {
            jda = null;
            setModuleState(ModuleState.ERROR);
            Logger.log(token);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: the token is invalid!)", invalidTokenException);
        } catch (InterruptedException e) {
            jda = null;
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: an InterruptedException has been received)", e);
        }

        registerCommands();
        registerListeners();

        if (!isReloading) setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Load: Successful!");
    }
    @Override
    protected synchronized void unloadModule(final boolean isReloading, final boolean bypassPreliminaryChecks) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Unload: Started!");
        if (!bypassPreliminaryChecks) moduleUnloadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.UNLOADING);

        if (jda != null) {
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(Duration.ofMinutes(1))) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                jda.shutdownNow();
            }
            jda = null;
            userBlacklist = null;
        }

        if (!isReloading) setModuleState(ModuleState.NOT_LOADED);
        Logger.log(MODULE_NAME + " Module Unload: Successful!");
    }

    // Module Methods
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void registerCommands() {
        jda.addEventListener(
                new PlayCommand(),
                new StopCommand(),
                new LoopCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new SkipCommand(),
                new ShutdownCommand(),
                new ListCommand()
        );
        CommandListUpdateAction commandUpdate = jda.updateCommands();
        commandUpdate.addCommands(Commands.slash(PlayCommand.NAME, PlayCommand.DESCRIPTION).addOption(OptionType.STRING, "track", "Name of the song or it's link.", true));
        commandUpdate.addCommands(Commands.slash(StopCommand.NAME, StopCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(LoopCommand.NAME, LoopCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(PauseCommand.NAME, PauseCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(ResumeCommand.NAME, ResumeCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(SkipCommand.NAME, SkipCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(ShutdownCommand.NAME, ShutdownCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(ListCommand.NAME, ListCommand.DESCRIPTION));
        commandUpdate.queue();
    }
    private void registerListeners() {
        InactivityListener.registerListener(jda);
        MasterListener.registerListener(jda);
        VoiceChannelListener.registerListener(jda);
    }
    private void loadBlacklist() throws ConfigurationModuleException {
        JSONArray blacklistedUserIDs = (JSONArray) ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.BLACKLIST);
        if (blacklistedUserIDs == null) throw new ConfigurationModuleException("Blacklist entry is not present in configuration map");
        userBlacklist = new ArrayList<>();
        for (int i=0; i < blacklistedUserIDs.length(); i++) {
            userBlacklist.add(blacklistedUserIDs.getString(i));
        }
    }
    public boolean isUserBlacklisted(@NotNull final String userID) {
        return userBlacklist.contains(userID);
    }
    public boolean isGuildSettingPresent(@NotNull final String GUID_ID, @NotNull final String KEY) throws SQLException {
        String query = "SELECT * FROM guild_settings WHERE setting_key=? AND guild_id=?;";
        PreparedStatement ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, KEY);
        ps.setString(2, GUID_ID);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            ps.close();
            return true;
        } else {
            ps.close();
            return false;
        }
    }
    public void writeGuildSetting(@NotNull final String GUILD_ID, @NotNull final String KEY, @NotNull final String VALUE) throws SQLException {
        String query;
        PreparedStatement ps;
        if (isGuildSettingPresent(GUILD_ID, KEY)) { // Update
            query = "UPDATE guild_settings SET setting_value=? WHERE setting_key=? AND guild_id=?;";
            ps = ModuleDatabase.getInstance().preparedStatement(query);
            if (ps == null) throw new SQLException("The database connection doesn't exist");
            ps.setString(1, VALUE);
            ps.setString(2, KEY);
            ps.setString(3, GUILD_ID);
        } else { // Insert
            query = "INSERT OR REPLACE INTO guild_settings (guild_id, setting_key, setting_value) VALUES (?, ?, ?);";
            ps = ModuleDatabase.getInstance().preparedStatement(query);
            if (ps == null) throw new SQLException("The database connection doesn't exist");
            ps.setString(1, GUILD_ID);
            ps.setString(2, KEY);
            ps.setString(3, VALUE);
        }
        ps.executeUpdate();
        ps.close();
    }
    public String readGuildSetting(@NotNull final String GUILD_ID, @NotNull final String KEY) throws SQLException {
        PreparedStatement ps;
        String query = "SELECT setting_value FROM guild_settings WHERE setting_key=? AND guild_id=?;";
        ps = ModuleDatabase.getInstance().preparedStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, KEY);
        ps.setString(2, GUILD_ID);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            String value = result.getString("setting_value");
            ps.close();
            return value;
        } else {
            ps.close();
            return null;
        }
    }

    // Instance
    private static ModuleJDA instance = null;
    private ModuleJDA() {
        super("JDA");
    }
    @NotNull
    public static ModuleJDA getInstance() {
        if (instance == null) instance = new ModuleJDA();
        return instance;
    }

    // JDA Defs
    public static final class Defs {

        // Gateway Intents
        public static final GatewayIntent[] GATEWAY_INTENTS = {
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES
        };

        // Enabled Cache Flags
        public static final CacheFlag[] ENABLED_CACHE_FLAGS = {
                CacheFlag.VOICE_STATE
        };

        // Disabled Cache Flags
        public static final CacheFlag[] DISABLED_CACHE_FLAGS = {
                CacheFlag.ACTIVITY,
                CacheFlag.EMOJI,
                CacheFlag.STICKER,
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ONLINE_STATUS,
                CacheFlag.SCHEDULED_EVENTS
        };

        // LavaPlayer Defs
        public static final int LAVAPLAYER_BUFFER_SIZE = 1024;

        // Hacker6329's Account ID
        public static final String MASTER_ACCOUNT_ID = "467835670761701376";
    }
}
