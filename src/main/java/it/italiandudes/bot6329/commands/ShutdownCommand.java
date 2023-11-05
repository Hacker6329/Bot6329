package it.italiandudes.bot6329.commands;

import it.italiandudes.bot6329.Bot6329;
import it.italiandudes.bot6329.util.Defs;
import it.italiandudes.bot6329.util.UserBlacklist;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class ShutdownCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "shutdown";
    public static final String DESCRIPTION = "Shutdown the bot remotely (only the Master can run this command)";

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
        if (!member.getId().equals(Defs.MASTER_ACCOUNT_ID)) {
            event.reply("Error: Only the Master can run this command.").setEphemeral(true).queue();
            return;
        }
        if (Bot6329.shutdown(true)) {
            event.reply("Remote Shutdown Procedure Initiated!").setEphemeral(true).queue();
        } else {
            event.reply("The bot is already shutting down! Please stand by...").setEphemeral(true).queue();
        }
    }
}
