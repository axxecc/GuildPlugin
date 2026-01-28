package com.guild.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * 兼容性调度器 - 支持Paper和Folia
 */
public class CompatibleScheduler {

    /**
     * 在主线程执行任务
     */
    public static void runTask(Plugin plugin, Runnable task) {
        // 直接使用 Paper 的全局区域调度
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    /**
     * 在指定位置执行任务
     */
    public static void runTask(Plugin plugin, Location location, Runnable task) {
        // 直接使用 Paper 的区域调度
        Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
    }

    /**
     * 在指定实体所在区域执行任务
     */
    public static void runTask(Plugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
    }

    /**
     * 延迟执行任务
     */
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        // 直接使用 Paper 的全局区域调度
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
    }

    /**
     * 在指定位置延迟执行任务
     */
    public static void runTaskLater(Plugin plugin, Location location, Runnable task, long delay) {
        // 直接使用 Paper 的区域调度
        Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
    }

    /**
     * 异步执行任务
     */
    public static void runTaskAsync(Plugin plugin, Runnable task) {
        // 直接使用 Paper 的异步调度器
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    /**
     * 重复执行任务
     */
    public static void runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        // 直接使用 Paper 的全局区域调度
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period);
    }

    /**
     * 检查是否在主线程
     */
    public static boolean isPrimaryThread() {
        try {
            // 直接使用Paper API的全局线程检查
            return Bukkit.isGlobalTickThread();
        } catch (Exception e) {
            // 如果Paper API不可用，回退到传统检查
            return Bukkit.isPrimaryThread();
        }
    }
}
