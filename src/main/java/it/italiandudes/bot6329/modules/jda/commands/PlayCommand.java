package it.italiandudes.bot6329.modules.jda.commands;

import it.italiandudes.bot6329.modules.jda.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.modules.jda.utils.BlacklistManager;
import it.italiandudes.bot6329.modules.jda.utils.GuildLocalization;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

public class PlayCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "play";
    public static final String DESCRIPTION = "Play a resource from a link in your voice channel";

    // Command Body
    @Override @SuppressWarnings("DuplicatedCode")
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        if (!event.getName().equals(NAME)) return;
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply(Localization.FALLBACK.localizeString(LocalizationKey.MUST_BE_IN_GUILD_TO_USE_THIS_COMMAND)).setEphemeral(true).queue();
            return;
        }
        String guildID = guild.getId();

        Member member = event.getMember();
        if (member == null) return;
        if (member.getUser().isBot()) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.CANT_USE_COMMAND_AS_BOT)).setEphemeral(true).queue();
            return;
        }
        if (!member.hasPermission(Permission.ADMINISTRATOR) && BlacklistManager.isUserBlacklisted(guildID, member.getUser().getId())) {
            event.reply("TITAN: SUCK IT").setEphemeral(true).queue();
            return;
        }
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (memberVoiceState == null || !memberVoiceState.inAudioChannel() || memberVoiceState.getChannel() == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.YOU_MUST_BE_IN_VOICE_CHANNEL)).setEphemeral(true).queue();
            return;
        }

        OptionMapping trackOption = event.getOption("track");
        if (trackOption == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MUST_PROVIDE_SONG_NAME_OR_LINK)).setEphemeral(true).queue();
            return;
        }
        String track = trackOption.getAsString();
        if (!PlayerManager.isURL(track)) {
            track = "ytsearch:" + track + " audio";
        }

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel() || selfVoiceState.getChannel() == null) {
            guild.getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            guild.getAudioManager().setSelfDeafened(true);
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.JOINING_IN_VC, memberVoiceState.getChannel().getName())).queue();
        } else if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MUST_BE_IN_SAME_VOICE_CHANNEL)).setEphemeral(true).queue();
            return;
        } else {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.LOADING)).setEphemeral(true).queue();
        }

        PlayerManager playerManager = PlayerManager.getInstance();
        playerManager.loadAndPlay(event.getChannel().asTextChannel(), track);
    }
}
