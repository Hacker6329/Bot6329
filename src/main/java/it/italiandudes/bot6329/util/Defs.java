package it.italiandudes.bot6329.util;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public final class Defs {

    // Gateway Intents
    public static final GatewayIntent[] GATEWAY_INTENTS = {
            GatewayIntent.GUILD_VOICE_STATES
    };

    // Cache Flags
    public static final CacheFlag[] CACHE_FLAGS = {
            CacheFlag.VOICE_STATE
    };

    // LavaPlayer Defs
    public static final class LavaPlayer {
        public static final int BUFFER_SIZE = 1024;
    }

    public static final String TOKEN = "MTE2OTU5MDgyNTM2MjE0MTIwNQ.GfT46s.kfQBB31_4SJrrUx9fKIiNKH6W_dHbsyk833LPk";

}
