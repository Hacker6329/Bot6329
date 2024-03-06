package it.italiandudes.bot6329.modules.jda.commands;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import it.italiandudes.bot6329.modules.jda.lavaplayer.PlayerManager;
import it.italiandudes.bot6329.modules.jda.lavaplayer.TrackScheduler;
import it.italiandudes.bot6329.modules.jda.utils.BlacklistManager;
import it.italiandudes.bot6329.modules.jda.utils.GuildLocalization;
import it.italiandudes.bot6329.modules.localization.Localization;
import it.italiandudes.bot6329.modules.localization.LocalizationKey;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SkipCommand extends ListenerAdapter {

    // Attributes
    public static final String NAME = "skip";
    public static final String DESCRIPTION = "Skip the current track and play the next one if present";
    private EventWaiter eventWaiter;

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

        Member self = guild.getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState == null || !selfVoiceState.inAudioChannel() || selfVoiceState.getChannel() == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.NOT_IN_AUDIO_CHANNEL)).setEphemeral(true).queue();
            return;
        } else if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.MUST_BE_IN_SAME_VOICE_CHANNEL)).setEphemeral(true).queue();
            return;
        }

        AudioTrack track = PlayerManager.getInstance().getMusicManager(guild).getScheduler().getPlayingAudioTrack();
        TrackScheduler scheduler = PlayerManager.getInstance().getMusicManager(guild).getScheduler();
        if (track == null) {
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.NOT_PLAYING_TRACK)).setEphemeral(true).queue();
            return;
        }

        OptionMapping vetoOption = event.getOption("admins_have_veto_power");
        boolean adminHasVetoPower = vetoOption != null && vetoOption.getAsBoolean();

        long humansInChannel = selfVoiceState.getChannel().getMembers().stream().filter(m -> !m.getUser().isBot()).count();
        if (humansInChannel < 2) {
            scheduler.nextTrack();
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIPPED)).queue();
            return;
        }

        if (member.hasPermission(Permission.ADMINISTRATOR) && adminHasVetoPower) {
            scheduler.nextTrack();
            event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIPPED_VETO, member.getAsMention())).queue();
            return;
        }

        long voteThreshold = humansInChannel%2==0?humansInChannel/2:((humansInChannel-1)/2)+1;

        TextChannel channel = event.getChannel().asTextChannel();
        UnicodeEmoji upVoteEmoji = Emoji.fromUnicode("\uD83D\uDC4D");
        UnicodeEmoji downVoteEmoji = Emoji.fromUnicode("\uD83D\uDC4E");
        UnicodeEmoji vetoPowerEmoji = adminHasVetoPower?Emoji.fromUnicode("\u2705"):Emoji.fromUnicode("\u274C");
        event.reply(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIP_VOTE, track.getInfo().title, track.getInfo().author, String.valueOf(voteThreshold), vetoPowerEmoji.getFormatted())).queue(e -> e.retrieveOriginal().queue(message -> {
            message.addReaction(upVoteEmoji).queue();
            message.addReaction(downVoteEmoji).queue();

            AtomicBoolean adminVetoSuccess = new AtomicBoolean(false);
            AtomicBoolean adminVetoFail = new AtomicBoolean(false);
            AtomicReference<String> vetoAdminName = new AtomicReference<>(null);

            if (eventWaiter == null) {
                eventWaiter = new EventWaiter();
                event.getJDA().addEventListener(eventWaiter);
            }

            eventWaiter.waitForEvent(MessageReactionAddEvent.class, (waitEvent) -> {
                if (message.getIdLong() != waitEvent.getMessageIdLong()) return false;
                Member voteMember = waitEvent.getMember();
                if (adminHasVetoPower && voteMember != null && !voteMember.getUser().isBot() && voteMember.hasPermission(Permission.ADMINISTRATOR)) {
                    if (waitEvent.getReaction().getEmoji().asUnicode().equals(upVoteEmoji)) {
                        adminVetoSuccess.set(true);
                        vetoAdminName.set(voteMember.getAsMention());
                        return true;
                    } else if (waitEvent.getReaction().getEmoji().asUnicode().equals(downVoteEmoji)) {
                        adminVetoFail.set(true);
                        vetoAdminName.set(voteMember.getAsMention());
                        return true;
                    }
                }

                Message voteMessage = waitEvent.retrieveMessage().complete();
                MessageReaction upvotesReaction = voteMessage.getReaction(upVoteEmoji);
                MessageReaction downvotesReaction = voteMessage.getReaction(downVoteEmoji);

                int upvotes = upvotesReaction != null ? upvotesReaction.getCount()-1 : 0;
                int downvotes = downvotesReaction != null ? downvotesReaction.getCount()-1 : 0;

                return upvotes >= voteThreshold || downvotes >= voteThreshold;
            }, (successEvent) -> {
                if (adminHasVetoPower && adminVetoSuccess.get()) {
                    scheduler.nextTrack();
                    channel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIP_VOTE_VETO_SUCCESS, vetoAdminName.get())).queue();
                } else if (adminHasVetoPower && adminVetoFail.get()) {
                    channel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIP_VOTE_VETO_FAIL, vetoAdminName.get())).queue();
                } else {
                    if (!track.equals(PlayerManager.getInstance().getMusicManager(guild).getScheduler().getPlayingAudioTrack())) {
                        channel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIP_VOTE_TRACK_CHANGED)).queue();
                    } else {
                        Message voteMessage = successEvent.retrieveMessage().complete();
                        MessageReaction upvotesReaction = voteMessage.getReaction(upVoteEmoji);
                        MessageReaction downvotesReaction = voteMessage.getReaction(downVoteEmoji);

                        int upvotes = upvotesReaction != null ? upvotesReaction.getCount()-1 : 0;
                        int downvotes = downvotesReaction != null ? downvotesReaction.getCount()-1 : 0;
                        if (upvotes > downvotes) {
                            scheduler.nextTrack();
                            channel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIP_VOTE_SUCCESS, upvotes, downvotes)).queue();
                        } else {
                            channel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIP_VOTE_FAIL, downvotes, upvotes)).queue();
                        }
                    }
                }
            }, 30, TimeUnit.SECONDS, () -> channel.sendMessage(GuildLocalization.localizeString(guildID, LocalizationKey.TRACK_SKIP_VOTE_TIMEOUT)).queue());
        }));
    }
}
