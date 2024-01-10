package it.italiandudes.bot6329.console;

import it.italiandudes.bot6329.Bot6329;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.idl.common.StringHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ConsoleCommandHandler {

    // Console Command Handler
    public static boolean handleConsoleCommand(@Nullable String userInput) {
        if (userInput == null) userInput = "";
        String[] parsedCommand = StringHandler.parseString(userInput);
        String[] commandArgs = null;
        if (parsedCommand.length <= 1) {
            parsedCommand = new String[] {userInput};
        } else {
            commandArgs = Arrays.copyOfRange(parsedCommand, 1, parsedCommand.length);
        }
        ConsoleCommand consoleCommand = ConsoleCommand.getConsoleCommandByAlias(parsedCommand[0]);
        if (consoleCommand == null) {
            logUnknownMessage();
            return true;
        }

        switch (consoleCommand) {
            case STOP:
                Bot6329.InternalMethods.shutdown(false);
                return false;

            default:
                logUnimplementedMessage();
        }

        return true;
    }

    // Not implemented command message
    private static void logUnimplementedMessage() {
        Logger.log("This command is registered, but it's not implemented yet.");
    }

    // Unknown command default message
    private static void logUnknownMessage() {
        Logger.log("Unknown command! Type \"help\" or \"?\" to show the list of commands");
    }
}
