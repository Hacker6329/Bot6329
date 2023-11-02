package it.italiandudes.bot6329.util;

import it.italiandudes.bot6329.commands.LoopCommand;
import it.italiandudes.bot6329.commands.PlayCommand;
import it.italiandudes.bot6329.commands.StopCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

public final class CommandManager {

    // Attributes
    private static boolean commandsRegistered = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void registerCommands(@NotNull final JDA jda) {
        if (commandsRegistered) return;
        jda.addEventListener(new PlayCommand(), new StopCommand(), new LoopCommand());
        CommandListUpdateAction commandUpdate = jda.updateCommands();
        commandUpdate.addCommands(Commands.slash(PlayCommand.NAME, PlayCommand.DESCRIPTION).addOption(OptionType.STRING, "track", "Name of the song or it's link.", true));
        commandUpdate.addCommands(Commands.slash(StopCommand.NAME, StopCommand.DESCRIPTION));
        commandUpdate.addCommands(Commands.slash(LoopCommand.NAME, LoopCommand.DESCRIPTION));
        commandUpdate.queue();
        commandsRegistered = true;
    }

}
