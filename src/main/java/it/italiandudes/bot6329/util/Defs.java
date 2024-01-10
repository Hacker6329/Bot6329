package it.italiandudes.bot6329.util;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public final class Defs {

    // Gateway Intents
    public static final GatewayIntent[] GATEWAY_INTENTS = {
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES
    };

    // Enabled Cache Flags
    public static final CacheFlag[] ENABLED_CACHE_FLAGS = {
            CacheFlag.VOICE_STATE
    };

    // Disabled Cache Flags
    public static final CacheFlag[] DISABLED_CACHE_FLAGS = {
            CacheFlag.ACTIVITY,
            CacheFlag.EMOJI,
            CacheFlag.STICKER,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ONLINE_STATUS,
            CacheFlag.SCHEDULED_EVENTS
    };

    // LavaPlayer Defs
    public static final class LavaPlayer {
        public static final int BUFFER_SIZE = 1024;
    }

    // Hacker6329's Account ID
    public static final String MASTER_ACCOUNT_ID = "467835670761701376";

    // Bot Token: DO NOT SHARE IT FOR ANY REASON
    public static final String TOKEN = "MTE2OTU5MDgyNTM2MjE0MTIwNQ.GfT46s.kfQBB31_4SJrrUx9fKIiNKH6W_dHbsyk833LPk";
}
