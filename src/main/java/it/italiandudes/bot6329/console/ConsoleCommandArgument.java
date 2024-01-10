package it.italiandudes.bot6329.console;

import org.jetbrains.annotations.NotNull;

public final class ConsoleCommandArgument {

    // Attributes
    @NotNull private final String name;
    @NotNull private final Class<?> type;
    @NotNull private final String description;

    // Constructors
    public ConsoleCommandArgument(@NotNull final String name, @NotNull final Class<?> type, @NotNull final String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    // Methods
    @NotNull
    public String getName() {
        return name;
    }
    @NotNull
    public Class<?> getType() {
        return type;
    }
    @NotNull
    public String getDescription() {
        return description;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsoleCommandArgument)) return false;

        ConsoleCommandArgument that = (ConsoleCommandArgument) o;

        if (!getType().equals(that.getType())) return false;
        if (!getName().equals(that.getName())) return false;
        return getDescription().equals(that.getDescription());
    }
    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getDescription().hashCode();
        return result;
    }
    @Override
    public String toString() {
        return name + " [" + type.getName() + "] - " + description;
    }
}
