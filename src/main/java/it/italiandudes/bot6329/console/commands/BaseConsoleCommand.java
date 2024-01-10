package it.italiandudes.bot6329.console.commands;

import it.italiandudes.bot6329.console.ConsoleCommand;
import it.italiandudes.bot6329.console.ConsoleCommandArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class BaseConsoleCommand {

    // Attributes
    @NotNull protected final ConsoleCommand command;
    @Nullable protected final ConsoleCommandArgument[] arguments;
    @NotNull protected final String description;

    // Constructors
    public BaseConsoleCommand(@NotNull final ConsoleCommand command, @Nullable final ConsoleCommandArgument[] arguments, @NotNull final String description) {
        this.command = command;
        this.arguments = arguments;
        this.description = description;
    }

    // Methods
    @NotNull
    public ConsoleCommand getCommand() {
        return command;
    }
    @Nullable
    public ConsoleCommandArgument[] getArguments() {
        return arguments;
    }
    @NotNull
    public String getDescription() {
        return description;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseConsoleCommand)) return false;

        BaseConsoleCommand that = (BaseConsoleCommand) o;

        if (getCommand() != that.getCommand()) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(getArguments(), that.getArguments())) return false;
        return getDescription().equals(that.getDescription());
    }
    @Override
    public int hashCode() {
        int result = getCommand().hashCode();
        result = 31 * result + Arrays.hashCode(getArguments());
        result = 31 * result + getDescription().hashCode();
        return result;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(command.aliases[0]).append(":\n");
        builder.append('\t').append("Aliases: ");
        for (int i=0; i < command.aliases.length; i++) {
            builder.append(command.aliases[i]);
            if (i+1 < command.aliases.length) {
                builder.append(" | ");
            }
        }
        builder.append('\n');
        builder.append('\t').append("Arguments: ").append((arguments!=null?arguments.length:0)).append('\n');
        if (arguments != null) {
            for (int i=0; i < arguments.length; i++) {
                builder.append("\t\t").append(i).append(". ").append(arguments[i]).append('\n');
            }
        }
        builder.append('\t').append("Description: ").append(description);
        return builder.toString();
    }
    public abstract boolean execute(@NotNull final String[] arguments);
}
