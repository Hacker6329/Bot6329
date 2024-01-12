package it.italiandudes.bot6329.modules.console.commands;

import org.jetbrains.annotations.NotNull;

public class HelpConsoleCommand extends BaseConsoleCommand {

    public HelpConsoleCommand() {
        super(
                "help",
                "help [command]",
                "Show the synopsis of all commands or the documentation of the specified command."
        );
    }

    // Command Implementation
    @Override
    public int execute(@NotNull String[] arguments) {
        return 0;
    }
}
