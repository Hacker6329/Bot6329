package it.italiandudes.bot6329.modules.jda.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import it.italiandudes.bot6329.modules.jda.GuildLocalization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public final class PlayerManager {

    // Instance
    private static PlayerManager INSTANCE;

    // Get Instance
    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    // Attributes
    @NotNull private final HashMap<Long, GuildMusicManager> musicManagers;
    @NotNull private final AudioPlayerManager audioPlayerManager;

    // Private Constructor
    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    // Methods
    public void deleteMusicManager(@Nullable final Guild guild) {
        if (guild == null) return;
        GuildMusicManager manager = musicManagers.get(guild.getIdLong());
        if (manager != null) {
            manager.getScheduler().clearQueueAndSettings();
            musicManagers.remove(guild.getIdLong());
        }
    }
    public GuildMusicManager getMusicManager(@NotNull final Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildID) -> {
           final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
           guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
           return guildMusicManager;
        });
    }
    public void loadAndPlay(@NotNull final TextChannel textChannel, @NotNull final String trackURL) {
        String guildID = textChannel.getGuild().getId();
        final GuildMusicManager musicManager = getMusicManager(textChannel.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.getScheduler().queue(track);
                textChannel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_ADDED_TO_QUEUE, track.getInfo().title, track.getInfo().author)).queue();
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if (!tracks.isEmpty()) {
                    musicManager.getScheduler().queue(tracks.get(0));
                    textChannel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_ADDED_TO_QUEUE, tracks.get(0).getInfo().title, tracks.get(0).getInfo().author)).queue();
                }
            }
            @Override
            public void noMatches() {
                textChannel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_NO_MATCHES)).queue();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                textChannel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_LOAD_FAILED)).queue();
            }
        });
    }

}
