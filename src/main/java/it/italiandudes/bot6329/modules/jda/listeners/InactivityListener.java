package it.italiandudes.bot6329.modules.jda.listeners;

import it.italiandudes.bot6329.modules.jda.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class InactivityListener extends ListenerAdapter {

    // Attributes
    private static final HashMap<String, ScheduledExecutorService> INACTIVITY_SCHEDULER_MAP = new HashMap<>();

    // Scheduler Methods
    private void onUserLeaveChannel(@NotNull final VoiceChannel channel) {
        INACTIVITY_SCHEDULER_MAP.get(channel.getGuild().getId()).schedule(() -> {
            Guild guild = channel.getGuild();
            List<Member> members = new ArrayList<>(channel.getMembers());
            members.removeIf(m -> m.getUser().isBot());
            if (members.isEmpty()) {
                PlayerManager.getInstance().getMusicManager(guild).getScheduler().clearQueueAndSettings();
                guild.getAudioManager().closeAudioConnection();
            }
        }, 1, TimeUnit.MINUTES);
    }
    private void onUserJoinChannel(@NotNull final VoiceChannel channel) {
        List<Member> members = new ArrayList<>(channel.getMembers());
        members.removeIf(m -> m.getUser().isBot());
        if (members.isEmpty()) return;
        INACTIVITY_SCHEDULER_MAP.get(channel.getGuild().getId()).shutdownNow();
        INACTIVITY_SCHEDULER_MAP.put(channel.getGuild().getId(), Executors.newScheduledThreadPool(1));
    }

    // On bot server leave
    @Override
    public void onGuildLeave(@NotNull final GuildLeaveEvent event) {
        try {
            INACTIVITY_SCHEDULER_MAP.get(event.getGuild().getId()).shutdownNow();
        } catch (Exception ignored) {}
        INACTIVITY_SCHEDULER_MAP.remove(event.getGuild().getId());
    }

    // On bot server join
    @Override
    public void onGuildJoin(@NotNull final GuildJoinEvent event) {
        INACTIVITY_SCHEDULER_MAP.put(event.getGuild().getId(), Executors.newScheduledThreadPool(1));
    }

    // Interface Method Listener
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        GuildVoiceState botVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (botVoiceState == null || botVoiceState.getChannel() == null) {
            return;
        }
        VoiceChannel botChannel = botVoiceState.getChannel().asVoiceChannel();
        if (event.getChannelJoined() != null && event.getChannelJoined().equals(botChannel)) {
            onUserJoinChannel(event.getChannelJoined().asVoiceChannel());
            return;
        }
        if (event.getChannelLeft() != null && event.getChannelLeft().equals(botChannel)) {
            onUserLeaveChannel(event.getChannelLeft().asVoiceChannel());
        }
    }

    // Register Method
    public static void registerListener(@NotNull final JDA jda) {
        for (Guild guild : jda.getGuilds()) {
            INACTIVITY_SCHEDULER_MAP.put(guild.getId(), Executors.newScheduledThreadPool(1));
        }
        jda.addEventListener(new InactivityListener());
    }
}
