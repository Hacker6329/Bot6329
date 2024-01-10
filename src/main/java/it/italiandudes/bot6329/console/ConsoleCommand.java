package it.italiandudes.bot6329.console;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public enum ConsoleCommand {
    HELP(new String[]{"help", "?"}),
    STOP(new String[]{"stop", "exit", "shutdown"})
    ;

    // Attributes
    @Nullable public final String[] aliases;

    // Constructors
    ConsoleCommand(@NotNull final String alias) {
        this.aliases = new String[]{alias};
    }
    ConsoleCommand(@NotNull final String[] aliases) {
        this.aliases = aliases;
    }

    // Methods
    @Nullable
    public static ConsoleCommand getConsoleCommandByAlias(@NotNull final String alias) {
        for (ConsoleCommand command : ConsoleCommand.values()) {
            if (Arrays.stream(command.aliases).anyMatch(Predicate.isEqual(alias))) return command;
        }
        return null;
    }
    @NotNull
    public ArrayList<String> getAliasesAsList() {
        return new ArrayList<>(Arrays.asList(aliases));
    }
}
