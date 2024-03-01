package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.jda.utils.BlacklistManager;
import it.italiandudes.bot6329.modules.jda.utils.GuildLocalization;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class BlacklistCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "blacklist";
    public static final String DESCRIPTION = "Blacklist manager";
    public static final String SUBCOMMAND_ENABLE = "enable";
    public static final String SUBCOMMAND_DISABLE = "disable";
    public static final String SUBCOMMAND_ADD = "add";
    public static final String SUBCOMMAND_REMOVE = "remove";

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
        if (BlacklistManager.isUserBlacklisted(guildID, member.getUser().getId())) {
            event.reply("TITAN: SUCK IT").setEphemeral(true).queue();
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
            case SUBCOMMAND_ADD -> subcommandAdd(event, guildID);
            case SUBCOMMAND_REMOVE -> subcommandRemove(event,guildID);
            default -> event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.INVALID_SUBCOMMAND)).setEphemeral(true).queue();
        }
    }

    // Subcommands
    private static void subcommandEnable(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        try {
            if (BlacklistManager.enableGuildBlacklist(guildID)) {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_ENABLED)).queue();
            } else {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_ALREADY_ENABLED)).queue();
            }
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
    private static void subcommandDisable(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        try {
            if (BlacklistManager.disableGuildBlacklist(guildID)) {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_DISABLED)).queue();
            } else {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_ALREADY_DISABLED)).queue();
            }
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
    private static void subcommandAdd(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        OptionMapping userMapping = event.getOption("user");
        if (userMapping == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MISSING_PARAMETER, "user")).setEphemeral(true).queue();
            return;
        }
        User user = userMapping.getAsUser();
        try {
            if (BlacklistManager.addUserToGuildBlacklist(guildID, user.getId())) {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_BLACKLISTED, user.getAsMention())).queue();
            } else {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_ALREADY_BLACKLISTED, user.getAsMention())).queue();
            }
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
    private static void subcommandRemove(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        OptionMapping userMapping = event.getOption("user");
        if (userMapping == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MISSING_PARAMETER, "user")).setEphemeral(true).queue();
            return;
        }
        User user = userMapping.getAsUser();
        try {
            if (BlacklistManager.removeUserFromGuildBlacklist(guildID, user.getId())) {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_UNBLACKLISTED, user.getAsMention())).queue();
            } else {
                event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.BLACKLIST_NOT_BLACKLISTED, user.getAsMention())).queue();
            }
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }
}
