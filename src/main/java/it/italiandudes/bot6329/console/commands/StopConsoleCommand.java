package it.italiandudes.bot6329.console.commands;

import it.italiandudes.bot6329.Bot6329;
import it.italiandudes.bot6329.console.ConsoleCommand;
import org.jetbrains.annotations.NotNull;

public final class StopConsoleCommand extends BaseConsoleCommand {

    // Constructors
    public StopConsoleCommand() {
        super(ConsoleCommand.STOP, null, "Shutdown the bot");
    }

    // Methods
    @Override
    public boolean execute(@NotNull String[] arguments) {
        Bot6329.InternalMethods.shutdown(false);
        return true;
    }
}
