package it.italiandudes.bot6329.modules.jda.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.database.entries.DatabaseGuildSettings;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.modules.jda.utils.GuildLocalization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
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
           try {
               String volumeStr = ModuleJDA.getInstance().readGuildSetting(guild.getId(), DatabaseGuildSettings.KEY_VOLUME);
               int volume = ModuleJDA.Defs.DEFAULT_VOLUME;
               if (volumeStr != null) {
                   try {
                       volume = Integer.parseInt(volumeStr);
                   } catch (NumberFormatException e) {
                       ModuleJDA.getInstance().writeGuildSetting(guild.getId(), DatabaseGuildSettings.KEY_VOLUME, String.valueOf(volume));
                   }
               }
               guildMusicManager.getScheduler().setVolume(volume);
           } catch (SQLException e) {
               Logger.log(e);
               ModuleManager.emergencyShutdownBot();
           }
           return guildMusicManager;
        });
    }
    public static boolean isURL(@NotNull final String URL) {
        try {
            new URI(URL);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
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
                if (tracks.isEmpty()) {
                    textChannel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.EMPTY_PLAYLIST)).queue();
                } else if (isURL(trackURL)) {
                    if (trackURL.contains("youtube") && trackURL.contains("list")) {
                        for (AudioTrack track : tracks) {
                            musicManager.getScheduler().queue(track);
                        }
                        textChannel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.PLAYLIST_ADDED_TO_QUEUE, tracks.size())).queue();
                    } else {
                        trackLoaded(tracks.get(0));
                    }
                } else {
                    trackLoaded(tracks.get(0));
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
