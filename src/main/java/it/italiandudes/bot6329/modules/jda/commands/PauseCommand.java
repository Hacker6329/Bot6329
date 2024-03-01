package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.jda.utils.BlacklistManager;
import it.italiandudes.bot6329.modules.jda.utils.GuildLocalization;
import it.italiandudes.bot6329.modules.jda.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.modules.jda.lavaplayer.TrackScheduler;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class PauseCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "pause";
    public static final String DESCRIPTION = "Pause the current playing track";

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

        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel() || memberVoiceState.getChannel() == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.YOU_MUST_BE_IN_VOICE_CHANNEL)).setEphemeral(true).queue();
            return;
        }

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel() || selfVoiceState.getChannel() == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.NOT_IN_AUDIO_CHANNEL)).setEphemeral(true).queue();
            return;
        } else if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MUST_BE_IN_SAME_VOICE_CHANNEL)).setEphemeral(true).queue();
            return;
        }

        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(guild).getScheduler();
        if (!scheduler.isPlayingTrack()) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.NOT_PLAYING_TRACK)).setEphemeral(true).queue();
            return;
        }
        if (scheduler.isPaused()) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.ALREADY_PAUSED)).setEphemeral(true).queue();
            return;
        }
        scheduler.setPaused(true);
        event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_PAUSED)).queue();
    }
}
