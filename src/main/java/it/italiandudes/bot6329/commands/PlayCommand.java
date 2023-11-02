package it.italiandudes.bot6329.commands;

import it.italiandudes.bot6329.lavaplayer.PlayerManager;
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

        OptionMapping linkOption = event.getOption("link");
        if (linkOption == null) {
            event.reply("You must provide the name of the song or the link!").queue();
            return;
        }
        String link = linkOption.getAsString();
        if (!isURL(link)) {
            link = "ytsearch:" + link + " audio";
        }

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel() || selfVoiceState.getChannel() == null) {
            guild.getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            guild.getAudioManager().setSelfDeafened(true);
            event.reply("Joining in **" + memberVoiceState.getChannel().getName() + "**").queue();
        } else if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You need to be in the same channel of the bot to use this command!").queue();
            return;
        } else {
            event.reply("Computing...").queue();
        }

        PlayerManager playerManager = PlayerManager.getInstance();
        playerManager.loadAndPlay(event.getChannel().asTextChannel(), link);
    }

    private boolean isURL(@NotNull final String URL) {
        try {
            new URI(URL);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
