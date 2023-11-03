package it.italiandudes.bot6329.commands;

import it.italiandudes.bot6329.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.util.UserBlacklist;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public class PlayCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "play";
    public static final String DESCRIPTION = "Play a resource from a link in your voice channel";

    // Command Body
    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        if (!event.getName().equals(NAME)) return;
        Member member = event.getMember();
        if (member == null) return;
        if (UserBlacklist.isUserBlacklisted(member.getUser().getId())) {
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

        OptionMapping trackOption = event.getOption("track");
        if (trackOption == null) {
            event.reply("You must provide the name of the song or the link!").setEphemeral(true).queue();
            return;
        }
        String track = trackOption.getAsString();
        if (!isURL(track)) {
            track = "ytsearch:" + track + " audio";
        }

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel() || selfVoiceState.getChannel() == null) {
            guild.getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            guild.getAudioManager().setSelfDeafened(true);
            event.reply("Joining in **" + memberVoiceState.getChannel().getName() + "**").queue();
        } else if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You need to be in the same channel of the bot to use this command!").setEphemeral(true).queue();
            return;
        } else {
            event.reply("Computing...").setEphemeral(true).queue();
        }

        PlayerManager playerManager = PlayerManager.getInstance();
        playerManager.loadAndPlay(event.getChannel().asTextChannel(), track);
    }

    // Methods
    private boolean isURL(@NotNull final String URL) {
        try {
            new URI(URL);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
