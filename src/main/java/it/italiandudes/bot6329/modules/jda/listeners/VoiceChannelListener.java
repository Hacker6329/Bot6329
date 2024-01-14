package it.italiandudes.bot6329.modules.jda.listeners;

import it.italiandudes.bot6329.modules.jda.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
public class VoiceChannelListener extends ListenerAdapter {

    // Clear queue and settings if the bot leave the channel
    @Override
    public void onGuildVoiceUpdate(@NotNull final GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) return;
        Member affectedMember = event.getMember();
        if (affectedMember.getUser().equals(event.getGuild().getSelfMember().getUser())) {
            PlayerManager.getInstance().getMusicManager(event.getGuild()).getScheduler().clearQueueAndSettings();
        }
    }

    // Refresh the guild music manager if the bot leave and join the guild
    @Override
    public void onGuildJoin(@NotNull final GuildJoinEvent event) {
        event.getGuild().getAudioManager().closeAudioConnection();
        PlayerManager.getInstance().deleteMusicManager(event.getGuild());
    }

    // Register Method
    public static void registerListener(@NotNull final JDA jda) {
        jda.addEventListener(new VoiceChannelListener());
    }
}
