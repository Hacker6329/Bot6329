package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.jda.GuildLocalization;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

public class LocalizationCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "localization";
    public static final String DESCRIPTION = "Localization manager";
    public static final String SUBCOMMAND_LIST = "list";
    public static final String SUBCOMMAND_GET = "get";
    public static final String SUBCOMMAND_SET = "set";

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
        if (ModuleJDA.getInstance().isUserBlacklisted(member.getUser().getId())) {
            event.reply("TITAN: SUCK IT").setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MISSING_SUBCOMMAND)).setEphemeral(true).queue();
            return;
        }

        switch (subcommandName) {
            case SUBCOMMAND_LIST -> subcommandList(event, guildID);
            case SUBCOMMAND_GET -> subcommandGet(event, guildID);
            case SUBCOMMAND_SET -> subcommandSet(event, guildID, member);
            default -> event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.INVALID_SUBCOMMAND)).setEphemeral(true).queue();
        }
    }

    private static void subcommandList(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        StringBuilder builder = new StringBuilder();
        builder.append(GuildLocalization.localizeString(guildID, LocalizationKey.LOCALIZATION_LIST)).append('\n');
        Localization[] localizations = Localization.values();
        for (int i=0; i<localizations.length; i++) {
            builder.append("- [**").append(localizations[i].toString()).append("**] ").append(localizations[i].EXTENDED_LANG);
            if (localizations[i] == Localization.FALLBACK) builder.append(" [DEFAULT]");
            if (i+1 < localizations.length) builder.append('\n');
        }
        event.reply(builder.toString()).queue();
    }
    private static void subcommandGet(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID) {
        Localization localization = GuildLocalization.getGuildLocalization(guildID);
        event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.LOCALIZATION_GET, localization.EXTENDED_LANG, localization.toString())).queue();
    }
    private static void subcommandSet(@NotNull final SlashCommandInteractionEvent event, @NotNull final String guildID, @NotNull final Member member) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MUST_BE_ADMINISTRATOR_TO_PERFORM_THIS_COMMAND)).setEphemeral(true).queue();
            return;
        }

        OptionMapping localeCodeOption = event.getOption("locale");
        if (localeCodeOption == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MUST_PROVIDE_LOCALE_CODE)).setEphemeral(true).queue();
            return;
        }
        String localeCode = localeCodeOption.getAsString();
        Localization localization = Localization.getLocalizationByLocale(localeCode);
        if (localization == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.PROVIDED_LOCALE_IS_INVALID)).setEphemeral(true).queue();
            return;
        }

        GuildLocalization.updateGuildLocalization(guildID, localization);
        event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.LOCALIZATION_UPDATED, localization.EXTENDED_LANG)).setEphemeral(true).queue();
    }
}
