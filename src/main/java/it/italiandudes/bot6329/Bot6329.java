package it.italiandudes.bot6329;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.bot6329.modules.console.ConsoleCommand;
import it.italiandudes.bot6329.throwables.errors.ModuleError;
import it.italiandudes.bot6329.throwables.exceptions.ModuleException;
import it.italiandudes.idl.common.InfoFlags;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.idl.common.StringHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

public final class Bot6329 {

    // Systemd Flag
    private static boolean isSystemd = false;
    public static boolean isSystemd() {
        return isSystemd;
    }

    // Main Method
    public static void main(String[] args) {

        // Initializing the logger (even if JDA has a Logger, I prefer mine)
        try {
            Logger.init();
        } catch (IOException e) {
            Logger.log("An error has occurred during Logger initialization, exit...");
            return;
        }

        // Handle Params
        if (Arrays.stream(args).anyMatch(Predicate.isEqual("-h")) || Arrays.stream(args).anyMatch(Predicate.isEqual("--help"))) {
            Logger.log("Parameters:");
            Logger.log("--help (or -h)     Show this help message.");
            Logger.log("--systemd          Prevent loading of ModuleConsole for bot use with systemd or any environment without stdin.");
            Logger.close();
            System.exit(1);
        } else if (Arrays.stream(args).anyMatch(Predicate.isEqual("--systemd"))) {
            isSystemd = true;
        }

        // Configure the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ModuleManager.shutdownBot();
            } catch (ModuleException e) {
                ModuleManager.emergencyShutdownBot();
            }
        }));
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            Logger.log(StringHandler.getStackTrace(e));
            ModuleManager.emergencyShutdownBot();
        });

        // Bot Initialization
        try {
            ModuleManager.initBot();
            Logger.log("Bot Status: ONLINE");
            Logger.log("Type \"" + ConsoleCommand.HELP.getName() + "\" to see the list of all commands.");
        } catch (ModuleException | ModuleError e) {
            if (!ModuleConfiguration.getInstance().isTokenMissing()) {
                ModuleManager.emergencyShutdownBot();
                Logger.log(e);
            } else {
                Logger.log(e.getMessage(), new InfoFlags(true, true));
            }
            Logger.close();
        }
    }
}
