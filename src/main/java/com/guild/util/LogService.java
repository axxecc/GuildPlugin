package com.guild.util;

import com.guild.GuildPlugin;

import java.util.logging.Level;

public class LogService {
    private static GuildPlugin plugin;
    private static boolean debugEnabled = false;

    public static void init(GuildPlugin pluginInstance) {
        if (pluginInstance == null) {
            throw new IllegalArgumentException("Plugin instance cannot be null");
        }
        plugin = pluginInstance;
    }

    /**
     * 检查调试模式是否启用
     */
    public static boolean isDebugMode() {
        return debugEnabled;
    }

    /**
     * 设置调试模式
     */
    public static void setDebugMode(boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * 输出信息级别日志
     */
    public static void info(String message) {
        plugin.getLogger().info(message);
    }

    /**
     * 输出警告级别日志
     */
    public static void warning(String message) {
        plugin.getLogger().warning(message);
    }

    /**
     * 输出警告级别日志
     */
    public static void severe(String message) {
        plugin.getLogger().severe(message);
    }

    /**
     * 输出调试级别日志（仅在调试模式启用时输出）
     */
    public static void debug(String message) {
        if (debugEnabled) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * 记录异常信息
     */
    public static void error(String message, Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, message, throwable);
    }
}