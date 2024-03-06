package it.italiandudes.bot6329.modules.jda;

import it.italiandudes.bot6329.modules.BaseModule;
import it.italiandudes.bot6329.modules.ModuleState;
import it.italiandudes.bot6329.modules.configuration.ConfigurationMap;
import it.italiandudes.bot6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.bot6329.modules.database.ModuleDatabase;
import it.italiandudes.bot6329.modules.jda.commands.*;
import it.italiandudes.bot6329.modules.jda.listeners.GuildListener;
import it.italiandudes.bot6329.modules.jda.listeners.InactivityListener;
import it.italiandudes.bot6329.modules.jda.listeners.MasterListener;
import it.italiandudes.bot6329.modules.jda.listeners.VoiceChannelListener;
import it.italiandudes.bot6329.modules.jda.utils.BlacklistManager;
import it.italiandudes.bot6329.modules.jda.utils.GreetingsManager;
import it.italiandudes.bot6329.throwables.errors.ModuleError;
import it.italiandudes.bot6329.throwables.exceptions.ModuleException;
import it.italiandudes.bot6329.throwables.exceptions.module.configuration.ConfigurationModuleException;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;

public class ModuleJDA extends BaseModule {

    // Attributes
    private JDA jda = null;

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

        try {
            BlacklistManager.initBlacklist();
        } catch (SQLException e) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: an SQL error has occurred during blacklist service init)", e);
        }
        try {
            GreetingsManager.initGreetingsManager();
        } catch (SQLException e) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: an SQL error has occurred during greetings service init)", e);
        }

        try {
            JDABuilder jdaBuilder = JDABuilder.create(token, Arrays.asList(Defs.GATEWAY_INTENTS));
            jdaBuilder.enableCache(Arrays.asList(Defs.ENABLED_CACHE_FLAGS));
            jdaBuilder.disableCache(Arrays.asList(Defs.DISABLED_CACHE_FLAGS));
            jda = jdaBuilder.build().awaitReady();
        } catch (InvalidTokenException invalidTokenException) {
            jda = null;
            setModuleState(ModuleState.ERROR);
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
                new ListCommand(),
                new LocalizationCommand(),
                new VolumeCommand(),
                new BlacklistCommand(),
                new GreetingsCommand()
        );
        CommandListUpdateAction commandUpdate = jda.updateCommands();
        commandUpdate.addCommands(Commands.slash(PlayCommand.NAME, PlayCommand.DESCRIPTION).addOption(OptionType.STRING, "track", "Name of the song or it's link.", true));
        commandUpdate.addCommands(Commands.slash(StopCommand.NAME, StopCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(LoopCommand.NAME, LoopCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(PauseCommand.NAME, PauseCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(ResumeCommand.NAME, ResumeCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(SkipCommand.NAME, SkipCommand.DESCRIPTION).addOption(OptionType.BOOLEAN, "admins_have_veto_power", "Allows admins to have veto power on the vote.", false).addOption(OptionType.INTEGER, "amount", "Amount of tracks to skip", false));
        commandUpdate.addCommands(Commands.slash(ShutdownCommand.NAME, ShutdownCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(ListCommand.NAME, ListCommand.DESCRIPTION));
        SubcommandData localizationList = new SubcommandData(LocalizationCommand.SUBCOMMAND_LIST, "List all the available localizations.");
        SubcommandData localizationGet = new SubcommandData(LocalizationCommand.SUBCOMMAND_GET, "Get the guild localization.");
        SubcommandData localizationSet = new SubcommandData(LocalizationCommand.SUBCOMMAND_SET, "Set the guild localization.").addOption(OptionType.STRING, "locale", "The locale code.", true);
        commandUpdate.addCommands(Commands.slash(LocalizationCommand.NAME, LocalizationCommand.DESCRIPTION).addSubcommands(localizationList, localizationGet, localizationSet));
        SubcommandData volumeGet = new SubcommandData(VolumeCommand.SUBCOMMAND_GET, "Get the bot volume for this guild.");
        SubcommandData volumeSet = new SubcommandData(VolumeCommand.SUBCOMMAND_SET, "Set the bot volume for this guild.").addOption(OptionType.INTEGER, "volume", "The new volume value.", true);
        commandUpdate.addCommands(Commands.slash(VolumeCommand.NAME, VolumeCommand.DESCRIPTION).addSubcommands(volumeGet, volumeSet));
        SubcommandData blacklistEnable = new SubcommandData(BlacklistCommand.SUBCOMMAND_ENABLE, "Enable the blacklist for this guild.");
        SubcommandData blacklistDisable = new SubcommandData(BlacklistCommand.SUBCOMMAND_DISABLE, "Disable the blacklist for this guild.");
        SubcommandData blacklistAdd = new SubcommandData(BlacklistCommand.SUBCOMMAND_ADD, "Add a user into the guild blacklist.").addOption(OptionType.USER, "user", "The user to add into the blacklist.", true);
        SubcommandData blacklistRemove = new SubcommandData(BlacklistCommand.SUBCOMMAND_REMOVE, "Remove a user from the guild blacklist.").addOption(OptionType.USER, "user", "The user to remove from the blacklist.", true);
        commandUpdate.addCommands(Commands.slash(BlacklistCommand.NAME, BlacklistCommand.DESCRIPTION).addSubcommands(blacklistEnable, blacklistDisable, blacklistAdd, blacklistRemove));
        SubcommandData greetingsEnable = new SubcommandData("enable", "Enable the greetings system for this guild.");
        SubcommandData greetingsDisable = new SubcommandData("disable", "Disable the greetings system for this guild.");
        SubcommandData greetingsSetChannel = new SubcommandData("set_channel", "Set a new text channel for greetings in this guild..").addOption(OptionType.CHANNEL, "channel", "The new greetings channel", true);
        SubcommandData greetingsGetChannel = new SubcommandData("get_channel", "Get the current greetings text channel of this guild.");
        SubcommandData greetingsGetMessage = new SubcommandData("get_message", "Get the current greetings message of this guild.");
        SubcommandData greetingsSetMessage = new SubcommandData("set_message", "Set a new greetings message for this guild.").addOption(OptionType.STRING, "message", "The new greetings message.", true);
        SubcommandData greetingsGetMessageConstants = new SubcommandData("get_message_constants", "Get all the greetings message constants.");
        commandUpdate.addCommands(Commands.slash(GreetingsCommand.NAME, GreetingsCommand.DESCRIPTION).addSubcommands(greetingsEnable, greetingsDisable, greetingsSetChannel, greetingsGetChannel, greetingsGetMessage, greetingsSetMessage, greetingsGetMessageConstants));
        commandUpdate.queue();
    }
    private void registerListeners() {
        InactivityListener.registerListener(jda);
        MasterListener.registerListener(jda);
        VoiceChannelListener.registerListener(jda);
        GuildListener.registerListener(jda);
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
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
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

        // Max Message Length
        public static final int MAX_MESSAGE_LENGTH = 2000;

        // Bot Default Volume
        public static final int DEFAULT_VOLUME = 50;

        // Hacker6329's Account ID
        public static final String MASTER_ACCOUNT_ID = "467835670761701376";
    }
}
