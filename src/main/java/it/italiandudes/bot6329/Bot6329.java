package it.italiandudes.bot6329;

import it.italiandudes.bot6329.listeners.ListenerManager;
import it.italiandudes.bot6329.util.CommandManager;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.bot6329.util.Defs;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Scanner;

public final class Bot6329 {

    // Attributes
    private static JDA jda = null;
    private static boolean shutdownInitiated = false;

    // Main Method
    public static void main(String[] args) throws InterruptedException {

        // Initializing the logger (even if JDA has a Logger, i prefer mine)
        try {
            Logger.init();
        } catch (IOException e) {
            Logger.log("An error has occurred during Logger initialization, exit...");
            return;
        }

        // Configure the shutdown hooks
        Runtime.getRuntime().addShutdownHook(new Thread(Logger::close));

        // Build & Configure the Bot Builder
        JDABuilder jdaBuilder = JDABuilder.create(Defs.TOKEN, Arrays.asList(Defs.GATEWAY_INTENTS));
        jdaBuilder.enableCache(Arrays.asList(Defs.ENABLED_CACHE_FLAGS));
        jdaBuilder.disableCache(Arrays.asList(Defs.DISABLED_CACHE_FLAGS));

        // Create Bot Instance
        jda = jdaBuilder.build().awaitReady();

        // Register Commands and Listeners
        CommandManager.registerCommands(jda);
        ListenerManager.registerListeners(jda);

        Logger.log("Bot Status: ONLINE");
        Logger.log("Type \"" + Defs.CONSOLE_SHUTDOWN + "\" to initialize bot shutdown");

        Scanner scan = new Scanner(System.in);
        while (true) {
            String consoleInput = scan.nextLine();
            if (consoleInput.equals(Defs.CONSOLE_SHUTDOWN)) {
                shutdown(false);
                break;
            }
        }
    }

    // Shutdown Method
    public static boolean shutdown(boolean remoteShutdown) {
        if (shutdownInitiated) {
            if (!remoteShutdown) Logger.log("Shutdown already initiated! Please stand by...");
            return false;
        }
        if (remoteShutdown) Logger.log("!!THE MASTER HAS INVOKED THE REMOTE SHUTDOWN!!");
        else Logger.log("Shutdown initiated: Please stand by...");
        shutdownInitiated = true;
        new Thread(() -> {
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(Duration.ofMinutes(1))) {
                    jda.shutdownNow();
                }
            } catch (InterruptedException e) {
                jda.shutdownNow();
            }
            Logger.log("Bot Status: OFFLINE");
            System.exit(0);
        }).start();
        return true;
    }
}
