package it.italiandudes.bot6329.modules.jda.listeners;

import it.italiandudes.bot6329.modules.ModuleManager;
import it.italiandudes.bot6329.modules.jda.utils.GreetingsManager;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class GuildListener extends ListenerAdapter {

    // On User Member Join
    @Override
    public void onGuildMemberJoin(@NotNull final GuildMemberJoinEvent event) {
        try {
            GreetingsManager.sendGreetings(event.getGuild(), event.getUser());
        } catch (SQLException e) {
            Logger.log(e);
            ModuleManager.emergencyShutdownBot();
        }
    }

    // Register Method
    public static void registerListener(@NotNull final JDA jda) {
        jda.addEventListener(new GuildListener());
    }
}
