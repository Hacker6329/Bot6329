package it.italiandudes.bot6329.commands;

import it.italiandudes.bot6329.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.lavaplayer.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class ResumeCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "resume";
    public static final String DESCRIPTION = "Resume the current playing track";

    // Command Body
    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        if (!event.getName().equals(NAME)) return;
        Member member = event.getMember();
        if (member == null) return;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel() || memberVoiceState.getChannel() == null) {
            event.reply("You need to be in a voice channel to use this command!").setEphemeral(true).queue();
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
        } else if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You need to be in the same channel of the bot to use this command!").setEphemeral(true).queue();
            return;
        }

        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(guild).getScheduler();
        if (!scheduler.isPlayingTrack()) {
            event.reply("I'm not playing a track!").setEphemeral(true).queue();
            return;
        }
        if (!scheduler.isPaused()) {
            event.reply("I'm not paused!").setEphemeral(true).queue();
            return;
        }
        scheduler.setPaused(false);
        event.reply("Track play resumed!").queue();
    }
}
