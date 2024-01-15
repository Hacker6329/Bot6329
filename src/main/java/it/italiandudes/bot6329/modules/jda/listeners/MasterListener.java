package it.italiandudes.bot6329.modules.jda.listeners;

import it.italiandudes.bot6329.modules.jda.GuildLocalization;
import it.italiandudes.bot6329.modules.jda.ModuleJDA;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import it.italiandudes.idl.common.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MasterListener extends ListenerAdapter {

    // Constructor
    public MasterListener(@NotNull final JDA jda) {
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            guild.retrieveMemberById(ModuleJDA.Defs.MASTER_ACCOUNT_ID).queue(master -> {
                TextChannel systemChannel = guild.getSystemChannel();
                if (master == null) {
                    if (systemChannel != null) {
                        systemChannel.sendMessage(GuildLocalization.localizeString(guild.getId(), LocalizationKey.QUITTING_MASTER_NOT_IN_GUILD)).queue(null, this::handleFailure);
                    }
                    guild.leave().queue(null, this::handleFailure);
                } else {
                    if (!master.hasPermission(Permission.ADMINISTRATOR)) {
                        if (systemChannel != null) {
                            systemChannel.sendMessage(GuildLocalization.localizeString(guild.getId(), LocalizationKey.QUITTING_MASTER_NOT_ADMIN_OR_OWNER)).queue(null, this::handleFailure);
                        }
                        guild.leave().queue(null, this::handleFailure);
                    }
                }
            }, this::handleFailure);
        }
    }

    // Master Presence Checker and Permission Checker
    @Override
    public void onGuildJoin(@NotNull final GuildJoinEvent event) {
        event.getGuild().retrieveMemberById(ModuleJDA.Defs.MASTER_ACCOUNT_ID).queue(member -> {
            Guild guild = event.getGuild();
            TextChannel systemChannel = guild.getSystemChannel();
            if (member == null) {
                if (systemChannel != null) {
                    systemChannel.sendMessage(GuildLocalization.localizeString(guild.getId(), LocalizationKey.QUITTING_MASTER_NOT_IN_GUILD)).queue(null, this::handleFailure);
                }
                guild.leave().queue(null, this::handleFailure);
                return;
            }
            if (member.hasPermission(Permission.ADMINISTRATOR)) return;
            if (systemChannel != null) {
                systemChannel.sendMessage(GuildLocalization.localizeString(guild.getId(), LocalizationKey.QUITTING_MASTER_NOT_ADMIN_OR_OWNER)).queue(null, this::handleFailure);
            }
            guild.leave().queue(null, this::handleFailure);
        }, this::handleFailure);
    }

    // Role Events
    @Override @SuppressWarnings("DuplicatedCode")
    public void onRoleDelete(@NotNull final RoleDeleteEvent event) {
        event.getGuild().retrieveMemberById(ModuleJDA.Defs.MASTER_ACCOUNT_ID).queue(member -> {
            TextChannel systemChannel = event.getGuild().getSystemChannel();
            if (member == null) {
                if (systemChannel != null) {
                    systemChannel.sendMessage(GuildLocalization.localizeString(event.getGuild().getId(), LocalizationKey.QUITTING_MASTER_NOT_IN_GUILD)).queue(null, this::handleFailure);
                }
                event.getGuild().leave().queue(null, this::handleFailure);
                return;
            }
            if (member.hasPermission(Permission.ADMINISTRATOR)) return;
            if (systemChannel != null) {
                systemChannel.sendMessage(GuildLocalization.localizeString(event.getGuild().getId(), LocalizationKey.QUITTING_MASTER_NOT_ADMIN_ANYMORE)).queue(null, this::handleFailure);
            }
            event.getGuild().leave().queue(null, this::handleFailure);
        }, this::handleFailure);
    }
    @Override @SuppressWarnings("DuplicatedCode")
    public void onRoleUpdatePermissions(@NotNull final RoleUpdatePermissionsEvent event) {
        event.getGuild().retrieveMemberById(ModuleJDA.Defs.MASTER_ACCOUNT_ID).queue(member -> {
            TextChannel systemChannel = event.getGuild().getSystemChannel();
            if (member == null) {
                if (systemChannel != null) {
                    systemChannel.sendMessage(GuildLocalization.localizeString(event.getGuild().getId(), LocalizationKey.QUITTING_MASTER_NOT_IN_GUILD)).queue(null, this::handleFailure);
                }
                event.getGuild().leave().queue(null, this::handleFailure);
                return;
            }
            if (member.hasPermission(Permission.ADMINISTRATOR)) return;
            if (systemChannel != null) {
                systemChannel.sendMessage(GuildLocalization.localizeString(event.getGuild().getId(), LocalizationKey.QUITTING_MASTER_NOT_ADMIN_ANYMORE)).queue(null, this::handleFailure);
            }
            event.getGuild().leave().queue(null, this::handleFailure);
        }, this::handleFailure);
    }

    // Master Is Administrator After Role Changes?
    @Override
    public void onGuildMemberRoleRemove(@NotNull final GuildMemberRoleRemoveEvent event) {
        Member member = event.getMember();
        if (!member.getId().equals(ModuleJDA.Defs.MASTER_ACCOUNT_ID)) return;
        if (member.hasPermission(Permission.ADMINISTRATOR)) return;
        TextChannel systemChannel = event.getGuild().getSystemChannel();
        if (systemChannel != null) {
            systemChannel.sendMessage(GuildLocalization.localizeString(event.getGuild().getId(), LocalizationKey.QUITTING_MASTER_NOT_ADMIN_ANYMORE)).queue(null, this::handleFailure);
        }
        event.getGuild().leave().queue(null, this::handleFailure);
    }

    // Master Leave Checker
    @Override
    public void onGuildMemberRemove(@NotNull final GuildMemberRemoveEvent event) {
        if (event.getUser().getId().equals(ModuleJDA.Defs.MASTER_ACCOUNT_ID)) {
            TextChannel systemChannel = event.getGuild().getSystemChannel();
            if (systemChannel != null) {
                systemChannel.sendMessage(GuildLocalization.localizeString(event.getGuild().getId(), LocalizationKey.QUITTING_MASTER_LEFT_THE_GUILD)).queue(null, this::handleFailure);
            }
            event.getGuild().leave().queue(null, this::handleFailure);
        }
    }

    // Failure Handler
    private void handleFailure(@NotNull final Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            switch (((ErrorResponseException) throwable).getErrorCode()) {
                case 10004: // Unknown Member
                case 10007: // Unknown Guild
                case 50001: // Missing Access
                    break;
                default:
                    Logger.log(throwable);
            }
        } else {
            Logger.log(throwable);
        }
    }

    // Register Method
    public static void registerListener(@NotNull final JDA jda) {
        jda.addEventListener(new MasterListener(jda));
    }
}
