package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.modules.jda.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.modules.jda.lavaplayer.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SkipCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "skip";
    public static final String DESCRIPTION = "Skip the current track and play the next one if present";

    // Command Body
    @Override @SuppressWarnings("DuplicatedCode")
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        if (!event.getName().equals(NAME)) return;
        Member member = event.getMember();
        if (member == null) return;
        if (member.getUser().isBot()) {
            event.reply("Can't use this command as a bot!").setEphemeral(true).queue();
            return;
        }
        if (ModuleJDA.getInstance().isUserBlacklisted(member.getUser().getId())) {
            event.reply("TITAN: SUCK IT").setEphemeral(true).queue();
            return;
        }
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

        scheduler.nextTrack();
        event.reply("Track skipped!").queue();
    }
}