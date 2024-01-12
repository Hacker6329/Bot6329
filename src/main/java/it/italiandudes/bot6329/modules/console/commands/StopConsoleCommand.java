package it.italiandudes.bot6329.modules.console.commands;

import it.italiandudes.bot6329.Bot6329;
import org.jetbrains.annotations.NotNull;

public final class StopConsoleCommand extends BaseConsoleCommand {

    // Constructors
    public StopConsoleCommand() {
        super(
                "stop",
                "stop",
                "Initiate the bot shutdown procedure."
        );
    }

    // Methods
    @Override
    public int execute(@NotNull String[] arguments) {
        Bot6329.InternalMethods.shutdown(false);
        return 0;
    }
}
