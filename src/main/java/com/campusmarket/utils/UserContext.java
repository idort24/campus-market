package com.campusmarket.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> PERMS = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setPerms(Set<String> perms) {
        PERMS.set(perms == null ? new HashSet<>() : new HashSet<>(perms));
    }

    public static Set<String> getPerms() {
        Set<String> p = PERMS.get();
        return p == null ? Collections.emptySet() : p;
    }

    public static void remove() {
        USER_ID.remove();
        PERMS.remove();
    }
}
