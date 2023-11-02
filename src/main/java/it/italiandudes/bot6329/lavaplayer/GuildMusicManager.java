package it.italiandudes.bot6329.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class GuildMusicManager {

    // Attributes
    @NotNull private final AudioPlayer audioPlayer;
    @NotNull private final TrackScheduler scheduler;
    @NotNull private final AudioPlayerSendHandler sendHandler;

    // Constructors
    public GuildMusicManager(@NotNull final AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(scheduler);
        this.sendHandler = new AudioPlayerSendHandler(audioPlayer);
    }

    // Methods
    @NotNull
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }
    @NotNull
    public TrackScheduler getScheduler() {
        return scheduler;
    }
    @NotNull
    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}