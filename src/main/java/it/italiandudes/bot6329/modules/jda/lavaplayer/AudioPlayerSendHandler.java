package it.italiandudes.bot6329.modules.jda.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class AudioPlayerSendHandler implements AudioSendHandler {

    // Attributes
    @NotNull private final AudioPlayer audioPlayer;
    @NotNull private final ByteBuffer buffer;
    @NotNull private final MutableAudioFrame frame;

    // Constructors
    public AudioPlayerSendHandler(@NotNull final AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(ModuleJDA.Defs.LAVAPLAYER_BUFFER_SIZE);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    // Methods
    @Override
    public boolean isOpus() {
        return true;
    }
    @Override
    public boolean canProvide() {
        return audioPlayer.provide(frame);
    }
    @Override
    public ByteBuffer provide20MsAudio() {
        return (ByteBuffer) buffer.flip();
    }
}
