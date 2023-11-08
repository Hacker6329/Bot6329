package it.italiandudes.bot6329.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import it.italiandudes.bot6329.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.lavaplayer.TrackScheduler;
import it.italiandudes.bot6329.util.UserBlacklist;
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
    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        if (!event.getName().equals(NAME)) return;
        Member member = event.getMember();
        if (member == null) return;
        if (member.getUser().isBot()) {
            event.reply("Can't use this command as a bot!").setEphemeral(true).queue();
            return;
        }
        if (UserBlacklist.isUserBlacklisted(member.getUser().getId())) {
            event.reply("TITAN: SUCK IT").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("You need to be in a guild to use this command!").setEphemeral(true).queue();
            return;
        }

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel() || selfVoiceState.getChannel() == null) {
            event.reply("I'm not in the audio channel!").setEphemeral(true).queue();
            return;
        }

        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(guild).getScheduler();
        StringBuilder messageBuilder = new StringBuilder();
        AudioTrackInfo playingAudioTrackInfo = scheduler.getPlayingAudioTrackInfo();

        if (playingAudioTrackInfo != null) {
            messageBuilder.append("**Currently Playing**: `").append(playingAudioTrackInfo.title).append("` by `").append(playingAudioTrackInfo.author).append("`\n");
        } else {
            messageBuilder.append("**Currently Playing**: Nothing\n");
        }
        messageBuilder.append("**In Queue**: ").append(scheduler.getQueueLength()).append("\n");
        ArrayList<AudioTrackInfo> trackInfos = scheduler.getQueueTrackInfo();
        for (int i=0; i<trackInfos.size(); i++) {
            AudioTrackInfo trackInfo = trackInfos.get(i);
            messageBuilder.append("**").append(i+1).append(".** `").append(trackInfo.title).append("` by `").append(trackInfo.author).append("`\n");
        }
        event.reply(messageBuilder.toString()).setEphemeral(true).queue();
    }
}
