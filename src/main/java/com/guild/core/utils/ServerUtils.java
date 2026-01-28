package com.guild.core.utils;

import org.bukkit.Bukkit;

/**
 * 服务器类型检测工具
 */
public class ServerUtils {

    private static ServerType serverType = null;

    /**
     * 检测服务器类型
     */
    public static ServerType getServerType() {
        if (serverType == null) {
            serverType = detectServerType();
        }
        return serverType;
    }

    /**
     * 获取服务器版本
     */
    public static String getServerVersion() {
        return Bukkit.getServer().getBukkitVersion();
    }

    /**
     * 检测服务器类型的具体实现
     */
    private static ServerType detectServerType() {
        try {
            // 尝试加载 Paper 的类
            Class.forName("io.papermc.paper.configuration.ServerConfiguration");
            return ServerType.PAPER;
        } catch (ClassNotFoundException e) {
            return ServerType.UNKNOWN;
        }
    }

    /**
     * 检查是否支持指定的API版本
     */
    public static boolean supportsApiVersion(String requiredVersion) {
        String serverVersion = getServerVersion();
        return compareVersions(serverVersion, requiredVersion) >= 0;
    }

    /**
     * 版本比较工具
     */
    private static int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("-")[0].split("\\.");
        String[] v2Parts = version2.split("-")[0].split("\\.");

        int maxLength = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < maxLength; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

            if (v1Part != v2Part) {
                return Integer.compare(v1Part, v2Part);
            }
        }

        return 0;
    }

    public enum ServerType {
        PAPER,
        UNKNOWN
    }
}
