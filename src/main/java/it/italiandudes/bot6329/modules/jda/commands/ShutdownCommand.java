package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.throwables.errors.ModuleError;
import it.italiandudes.bot6329.throwables.exceptions.ModuleException;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class ShutdownCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "shutdown";
    public static final String DESCRIPTION = "Shutdown the bot remotely (only the Master can run this command)";

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
        if (!member.getId().equals(ModuleJDA.Defs.MASTER_ACCOUNT_ID)) {
            event.reply("Error: Only the Master can run this command.").setEphemeral(true).queue();
            return;
        }
        Logger.log("!!!THE MASTER HAS INVOKED THE REMOTE SHUTDOWN!!!");
        event.reply("Remote Shutdown Procedure Initiated! Please stand by...").setEphemeral(true).queue();
        try {
            ModuleManager.shutdownBot();
        } catch (ModuleException | ModuleError e) {
            ModuleManager.emergencyShutdownBot();
        }
    }
}
