package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.jda.utils.BlacklistManager;
import it.italiandudes.bot6329.modules.jda.utils.GuildLocalization;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class GreetingsCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "welcome";
    public static final String DESCRIPTION = "Welcome message manager";
    public static final String SUBCOMMAND_ENABLE = "enable";
    public static final String SUBCOMMAND_DISABLE = "disable";
    public static final String SUBCOMMAND_SET_CHANNEL = "set_channel";
    public static final String SUBCOMMAND_GET_CHANNEL = "get_channel";
    public static final String SUBCOMMAND_GET_MESSAGE = "get_message";
    public static final String SUBCOMMAND_SET_MESSAGE =" set_message";

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
        if (!member.hasPermission(Permission.ADMINISTRATOR) && BlacklistManager.isUserBlacklisted(guildID, member.getUser().getId())) {
            event.reply("TITAN: SUCK IT").setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MISSING_SUBCOMMAND)).setEphemeral(true).queue();
            return;
        }

        switch (subcommandName) {
            case SUBCOMMAND_ENABLE -> subcommandEnable(event);
            case SUBCOMMAND_DISABLE -> subcommandDisable(event);
            case SUBCOMMAND_GET_MESSAGE -> subcommandGetMessage(event);
            case SUBCOMMAND_SET_MESSAGE -> subcommandSetMessage(event);
            case SUBCOMMAND_GET_CHANNEL -> subcommandGetChannel(event);
            case SUBCOMMAND_SET_CHANNEL -> subcommandSetChannel(event);
            default -> event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.INVALID_SUBCOMMAND)).setEphemeral(true).queue();
        }
    }

    // Subcommands
    private static void subcommandEnable(@NotNull final SlashCommandInteractionEvent event) {}
    private static void subcommandDisable(@NotNull final SlashCommandInteractionEvent event) {}
    private static void subcommandGetMessage(@NotNull final SlashCommandInteractionEvent event) {}
    private static void subcommandSetMessage(@NotNull final SlashCommandInteractionEvent event) {}
    private static void subcommandGetChannel(@NotNull final SlashCommandInteractionEvent event) {}
    private static void subcommandSetChannel(@NotNull final SlashCommandInteractionEvent event) {}
}
