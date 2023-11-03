package it.italiandudes.bot6329.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;

public final class UserBlacklist {

    // Blacklisted Users
    private static final String[] BLACKLISTED_USER_IDS = {
            "494131190610264085",
            "609376073070936084",
            "631028657263083520"
    };

    // Methods
    public static boolean isUserBlacklisted(@NotNull final String userID) {
        return Arrays.stream(BLACKLISTED_USER_IDS).anyMatch(Predicate.isEqual(userID));
    }

}
