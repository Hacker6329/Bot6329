package it.italiandudes.bot6329;

import it.italiandudes.bot6329.listeners.ListenerManager;
import it.italiandudes.bot6329.util.CommandManager;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.bot6329.util.Defs;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;
import java.util.Arrays;

public final class Bot6329 {

    // Main Method
    public static void main(String[] args) throws InterruptedException {

        // Initializing the logger
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
        JDA jda = jdaBuilder.build().awaitReady();

        // Register Commands and Listeners
        CommandManager.registerCommands(jda);
        ListenerManager.registerListeners(jda);
    }

}
