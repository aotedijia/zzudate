package org.example.zzudate.utils;

public class CurrentUser {
    private static final ThreadLocal<Long> USER_THREAD_LOCAL =
            new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_THREAD_LOCAL.set(userId);
    }

    public static Long getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }
}
