package com.guild.core.events;

import com.guild.util.LogService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 事件总线 - 统一管理插件内部事件
 */
public class EventBus {

    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    /**
     * 注册事件监听器
     */
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        LogService.info("注册事件监听器: " + eventType.getSimpleName());
    }

    /**
     * 取消注册事件监听器
     */
    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        CopyOnWriteArrayList<Consumer<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            LogService.info("取消注册事件监听器: " + eventType.getSimpleName());
        }
    }

    /**
     * 发布事件
     */
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        CopyOnWriteArrayList<Consumer<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<?> listener : eventListeners) {
                try {
                    ((Consumer<T>) listener).accept(event);
                } catch (Exception e) {
                    LogService.error("事件监听器执行失败: ", e);
                }
            }
        }
    }

    /**
     * 异步发布事件
     */
    public <T> void publishAsync(T event) {
        new Thread(() -> publish(event)).start();
    }

    /**
     * 清除所有监听器
     */
    public void clear() {
        listeners.clear();
        LogService.info("清除所有事件监听器");
    }

    /**
     * 获取监听器数量
     */
    public int getListenerCount(Class<?> eventType) {
        CopyOnWriteArrayList<Consumer<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }

    /**
     * 获取总监听器数量
     */
    public int getTotalListenerCount() {
        return listeners.values().stream().mapToInt(CopyOnWriteArrayList::size).sum();
    }
}
