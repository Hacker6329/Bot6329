package it.italiandudes.bot6329.modules.jda.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import it.italiandudes.bot6329.modules.jda.GuildLocalization;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
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

import java.util.ArrayList;

public final class ListCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "list";
    public static final String DESCRIPTION = "List the playing song and all the queued songs";

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

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel() || selfVoiceState.getChannel() == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.NOT_IN_AUDIO_CHANNEL)).setEphemeral(true).queue();
            return;
        }

        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(guild).getScheduler();
        StringBuilder messageBuilder = new StringBuilder();
        AudioTrackInfo playingAudioTrackInfo = scheduler.getPlayingAudioTrackInfo();

        if (playingAudioTrackInfo != null) {
            messageBuilder.append(GuildLocalization.localizeString(guildID, LocalizationKey.LIST_CURRENTLY_PLAYING, playingAudioTrackInfo.title, playingAudioTrackInfo.author));
        } else {
            messageBuilder.append(GuildLocalization.localizeString(guildID, LocalizationKey.LIST_CURRENTLY_PLAYING_NOTHING));
        }
        messageBuilder.append(GuildLocalization.localizeString(guildID, LocalizationKey.LIST_IN_QUEUE, scheduler.getQueueLength()));
        ArrayList<AudioTrackInfo> trackInfos = scheduler.getQueueTrackInfo();
        for (int i=0; i<trackInfos.size(); i++) {
            AudioTrackInfo trackInfo = trackInfos.get(i);
            messageBuilder.append(GuildLocalization.localizeString(guildID, LocalizationKey.LIST_IN_QUEUE_PART, i+1, trackInfo.title, trackInfo.author));
        }
        event.reply(messageBuilder.toString()).setEphemeral(true).queue();
    }
}
