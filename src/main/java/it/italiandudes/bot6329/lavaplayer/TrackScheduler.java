package it.italiandudes.bot6329.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.jetbrains.annotations.NotNull;

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
    public boolean isPlayingTrack() {
        return audioPlayer.getPlayingTrack() != null;
    }
    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
    }
    public void clearQueue() {
        audioPlayer.stopTrack();
        queue.clear();
        setLoopMode(false);
        setPaused(false);
    }
    public int getQueueLength() {
        return queue.size();
    }
    public void nextTrack() {
        this.audioPlayer.startTrack(this.queue.poll(), false);
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
