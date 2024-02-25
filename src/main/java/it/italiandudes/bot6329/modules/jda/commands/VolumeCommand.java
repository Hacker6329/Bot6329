package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.database.entries.DatabaseGuildSettings;
import it.italiandudes.bot6329.modules.jda.GuildLocalization;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.modules.jda.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class VolumeCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "volume";
    public static final String DESCRIPTION = "Volume manager";
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
            case SUBCOMMAND_GET -> subcommandGet(event, guild);
            case SUBCOMMAND_SET -> subcommandSet(event, guild, member);
            default -> event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.INVALID_SUBCOMMAND)).setEphemeral(true).queue();
        }
    }
    private static void subcommandGet(@NotNull final SlashCommandInteractionEvent event, @NotNull final Guild guild) {
        event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.VOLUME_GET, PlayerManager.getInstance().getMusicManager(guild).getScheduler().getVolume())).setEphemeral(true).queue();
    }
    private static void subcommandSet(@NotNull final SlashCommandInteractionEvent event, @NotNull final Guild guild, @NotNull final Member member) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.MUST_BE_ADMINISTRATOR_TO_PERFORM_THIS_COMMAND)).setEphemeral(true).queue();
            return;
        }

        OptionMapping volumeOption = event.getOption("volume");
        if (volumeOption == null) {
            event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.MUST_PROVIDE_VOLUME)).setEphemeral(true).queue();
            return;
        }
        int volume = volumeOption.getAsInt();
        if (volume < 0 || volume > 100) {
            event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.VOLUME_OUT_OF_BOUNDS)).setEphemeral(true).queue();
            return;
        }

        PlayerManager.getInstance().getMusicManager(guild).getScheduler().setVolume(volume);
        try {
            ModuleJDA.getInstance().writeGuildSetting(guild.getId(), DatabaseGuildSettings.KEY_VOLUME, String.valueOf(volume));
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
        event.reply(GuildLocalization.localizeString(guild.getId(), LocalizationKey.VOLUME_SET, volume)).queue();
    }
}
