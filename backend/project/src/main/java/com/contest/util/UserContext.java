package com.contest.util;

public final class UserContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUser(Long userId, String role) {
        USER_ID_HOLDER.set(userId);
        USER_ROLE_HOLDER.set(role);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static String getRole() {
        return USER_ROLE_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        USER_ROLE_HOLDER.remove();
    }
}
