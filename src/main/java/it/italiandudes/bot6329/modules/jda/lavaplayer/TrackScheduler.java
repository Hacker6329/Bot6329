package it.italiandudes.bot6329.modules.jda.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("unused")
public final class TrackScheduler extends AudioEventAdapter {

    // Attributes
    @NotNull private final AudioPlayer audioPlayer;
    @NotNull private final BlockingQueue<AudioTrack> queue;
    private boolean loopMode;
    private boolean isPaused;

    // Constructors
    public TrackScheduler(@NotNull final AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
        this.loopMode = false;
        this.isPaused = false;
    }

    // Methods
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void queue(@NotNull final AudioTrack track) {
        if (!this.audioPlayer.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }
    public boolean isLoopMode() {
        return loopMode;
    }
    public boolean isPaused() {
        return isPaused;
    }
    public void setPaused(boolean isPaused) {
        if (isPaused == this.isPaused) return;
        this.isPaused = isPaused;
        audioPlayer.setPaused(isPaused);
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isPlayingTrack() {
        return audioPlayer.getPlayingTrack() != null;
    }
    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
    }
    public void clearQueueAndSettings() {
        clearQueue();
        setPaused(false);
        setLoopMode(false);
    }
    public int getVolume() {
        return audioPlayer.getVolume();
    }
    public void setVolume(int volume) {
        if (volume < 0) volume = 0;
        if (volume > 100) volume = 100;
        audioPlayer.setVolume(volume);
    }
    public void clearQueue() {
        audioPlayer.stopTrack();
        queue.clear();
    }
    @Nullable
    public AudioTrack getPlayingAudioTrack() {
        if (audioPlayer.getPlayingTrack() == null) return null;
        return audioPlayer.getPlayingTrack();
    }
    @Nullable
    public AudioTrackInfo getPlayingAudioTrackInfo() {
        if (audioPlayer.getPlayingTrack() == null) return null;
        return audioPlayer.getPlayingTrack().getInfo();
    }
    @NotNull
    public ArrayList<AudioTrackInfo> getQueueTrackInfo() {
        ArrayList<AudioTrackInfo> trackInfos = new ArrayList<>();
        for (AudioTrack audioTrack : queue) {
            trackInfos.add(audioTrack.getInfo());
        }
        return trackInfos;
    }
    public int getQueueLength() {
        return queue.size();
    }
    public void nextTrack() {
        this.audioPlayer.startTrack(this.queue.poll(), false);
    }
    public void nextTrack(int amount) {
        for (int i=0; i < amount; i++) {
            this.queue.poll();
        }
        nextTrack();
    }
    @Override
    public void onTrackEnd(@NotNull final AudioPlayer player, @NotNull final AudioTrack track, @NotNull final AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (loopMode) {
                this.audioPlayer.startTrack(track.makeClone(), false);
            } else {
                nextTrack();
            }
        }
    }

}
