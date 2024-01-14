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

public final class Bot6329 {

    // Main Method
    public static void main(String[] args) {

        // Initializing the logger (even if JDA has a Logger, i prefer mine)
        try {
            Logger.init();
        } catch (IOException e) {
            Logger.log("An error has occurred during Logger initialization, exit...");
            return;
        }

        // Configure the shutdown hooks
        Runtime.getRuntime().addShutdownHook(new Thread(Logger::close));
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
