package it.italiandudes.bot6329.listeners;

import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public final class ListenerManager {

    public static void registerListeners(@NotNull final JDA jda) {
        InactivityListener.registerListener(jda);
    }

}
