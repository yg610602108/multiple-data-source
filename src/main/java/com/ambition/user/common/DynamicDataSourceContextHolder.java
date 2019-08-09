package com.ambition.user.common;

/**
 * @author Elewin
 * 2019-05-15 10:12 PM
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void putDataSource(String dbType) {
        CONTEXT_HOLDER.set(dbType);
    }

    public static String getDaraSource() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }
}
