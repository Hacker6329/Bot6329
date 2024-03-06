package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.modules.jda.utils.GreetingsManager;
import it.italiandudes.bot6329.modules.jda.utils.GuildLocalization;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class GreetingsCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "greetings";
    public static final String DESCRIPTION = "Welcome message manager";
    public static final String SUBCOMMAND_ENABLE = "enable";
    public static final String SUBCOMMAND_DISABLE = "disable";
    public static final String SUBCOMMAND_SET_CHANNEL = "set_channel";
    public static final String SUBCOMMAND_GET_CHANNEL = "get_channel";
    public static final String SUBCOMMAND_GET_MESSAGE = "get_message";
    public static final String SUBCOMMAND_SET_MESSAGE = "set_message";
    public static final String SUBCOMMAND_GET_MESSAGE_CONSTANTS = "get_message_constants";

    // Command Body
    @Override @SuppressWarnings("DuplicatedCode")
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        if (!event.getName().equals(NAME)) return;

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply(Localization.FALLBACK.localizeString(LocalizationKey.MUST_BE_IN_GUILD_TO_USE_THIS_COMMAND)).setEphemeral(true).queue();
            return;
        }
        String guildID = guild.getId();

        Member member = event.getMember();
        if (member == null) return;
        if (member.getUser().isBot()) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.CANT_USE_COMMAND_AS_BOT)).setEphemeral(true).queue();
            return;
        }

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MUST_BE_ADMINISTRATOR_TO_PERFORM_THIS_COMMAND)).setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MISSING_SUBCOMMAND)).setEphemeral(true).queue();
            return;
        }

        switch (subcommandName) {
            case SUBCOMMAND_ENABLE -> subcommandEnable(event, guildID);
            case SUBCOMMAND_DISABLE -> subcommandDisable(event, guildID);
            case SUBCOMMAND_GET_MESSAGE -> subcommandGetMessage(event, guildID);
            case SUBCOMMAND_SET_MESSAGE -> subcommandSetMessage(event, guildID);
            case SUBCOMMAND_GET_CHANNEL -> subcommandGetChannel(event, guild);
            case SUBCOMMAND_SET_CHANNEL -> subcommandSetChannel(event, guild);
            case SUBCOMMAND_GET_MESSAGE_CONSTANTS -> subcommandGetMessageConstants(event, guildID);
            default -> event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.INVALID_SUBCOMMAND)).setEphemeral(true).queue();
        }
    }

    // Subcommands
    private static void subcommandEnable(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        try {
            Boolean result = GreetingsManager.enableGuildGreetings(guildID);
            if (result == null) {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_NO_TEXT_CHANNEL_AVAILABLE)).setEphemeral(true).queue();
            } else if (result) {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_ENABLED)).queue();
            } else {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_ALREADY_ENABLED)).setEphemeral(true).queue();
            }
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
    private static void subcommandDisable(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        try {
            if (GreetingsManager.disableGuildGreetings(guildID)) {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_DISABLED)).queue();
            }else {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_ALREADY_DISABLED)).setEphemeral(true).queue();
            }
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
    private static void subcommandGetMessageConstants(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        event.reply(GreetingsManager.getGreetingsMessageConstants(guildID)).setEphemeral(true).queue();
    }
    private static void subcommandGetMessage(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        try {
            String message = GreetingsManager.getGuildGreetingsMessage(guildID);
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_SHOW_MESSAGE, MarkdownSanitizer.escape(message), message)).queue();
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
    private static void subcommandSetMessage(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        OptionMapping messageMapping = event.getOption("message");
        if (messageMapping == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MISSING_PARAMETER, "message")).setEphemeral(true).queue();
            return;
        }
        String message = messageMapping.getAsString();
        if (message.length() > ModuleJDA.Defs.MAX_MESSAGE_LENGTH) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_EXCEED_MAX_LENGTH, message.length(), ModuleJDA.Defs.MAX_MESSAGE_LENGTH)).setEphemeral(true).queue();
            return;
        }
        try {
            GreetingsManager.setGuildGreetingsMessage(guildID, message);
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.GREETINGS_MESSAGE_CHANGED, MarkdownSanitizer.escape(message), message)).queue();
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
    private static void subcommandGetChannel(@NotNull final SlashCommandInteractionEvent event, @NotNull final Guild guild) {
        TextChannel channel = GreetingsManager.getGreetingsChannel(guild);
        if (channel == null) {
            event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.GREETINGS_NO_TEXT_CHANNEL)).setEphemeral(true).queue();
        } else {
            event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.GREETINGS_SHOW_TEXT_CHANNEL, channel.getAsMention())).queue();
        }
    }
    private static void subcommandSetChannel(@NotNull final SlashCommandInteractionEvent event, @NotNull final Guild guild) {
        OptionMapping channelMapping = event.getOption("channel");
        if (channelMapping == null) {
            event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.MISSING_PARAMETER, "channel")).setEphemeral(true).queue();
            return;
        }
        TextChannel channel = channelMapping.getAsChannel().asTextChannel();
        try {
            GreetingsManager.setGreetingsChannel(guild, channel);
            event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.GREETINGS_TEXT_CHANNEL_CHANGED, channel.getAsMention())).queue();
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
}
