package it.italiandudes.bot6329;

import it.italiandudes.idl.common.Logger;
import it.italiandudes.bot6329.util.Defs;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.io.IOException;
import java.util.Arrays;

public final class Bot6329 {

    // Main Method
    public static void main(String[] args) throws InterruptedException, IOException {

        // Initializing the logger
        try {
            Logger.init();
        } catch (IOException e) {
            Logger.log("An error has occurred during Logger initialization, exit...");
            return;
        }

        // Configure the shutdown hooks
        Runtime.getRuntime().addShutdownHook(new Thread(Logger::close));

        JDABuilder jdaBuilder = JDABuilder.create(Defs.TOKEN, Arrays.asList(Defs.GATEWAY_INTENTS));

        JDA jda = jdaBuilder.build().awaitReady();
    }

}
