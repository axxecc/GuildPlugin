package com.guild.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息处理工具类 - 支持解析颜色代码和可点击消息
 */
public final class FormatUtil {

    // ==================== 字符串解析部分 ====================

    // 可点击消息的匹配模式，支持点击和悬停同时设置
    private static final Pattern CLICKABLE_PATTERN = Pattern.compile(
            "\\[([^]]+)]\\((run|suggest|copy|url):([^,]+)(?:,hover:([^)]+))?\\)"
    );
    // 仅悬停消息的匹配模式
    private static final Pattern HOVER_ONLY_PATTERN = Pattern.compile(
            "\\[([^]]+)]\\((hover):([^)]+)\\)"
    );

    private FormatUtil() {
    } // 防止实例化

    // ==================== 发送消息方法 ====================

    /**
     * 发送消息（支持&颜色代码和可点击消息）
     *
     * @param audience 接收者（玩家、控制台等）
     * @param message  消息内容（支持&颜色代码和[文本](type:value)格式）
     */
    public static void sendMessage(Audience audience, String message) {
        if (audience == null || message == null || message.isEmpty()) return;
        Component component = parse(message);
        audience.sendMessage(component);
    }

    /**
     * 发送ComponentBuilder构建的消息
     *
     * @param audience 接收者
     * @param builder  ComponentBuilder构建器
     */
    public static void sendMessage(Audience audience, ComponentBuilder builder) {
        if (audience == null || builder == null) return;
        audience.sendMessage(builder.build());
    }

    /**
     * 发送ComponentBuilder构建的消息
     *
     * @param audience 接收者
     * @param consumer ComponentBuilder配置器
     */
    public static void sendMessage(Audience audience, Consumer<ComponentBuilder> consumer) {
        if (audience == null || consumer == null) return;
        ComponentBuilder builder = ComponentBuilder.create();
        consumer.accept(builder);
        audience.sendMessage(builder.build());
    }

    // ==================== 解析方法 ====================

    /**
     * 解析消息文本，支持颜色代码和可点击消息
     *
     * @param text 原始文本，包含&颜色代码和[text](type:value)格式
     * @return 解析后的Component
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        List<MessageSegment> segments = parseClickableSegments(text);
        TextComponent.Builder builder = Component.text();

        for (MessageSegment segment : segments) {
            Component segmentComponent = parseColorCodes(segment.getText());

            if (segment.getClickEvent() != null) {
                segmentComponent = segmentComponent.clickEvent(segment.getClickEvent());
            }

            if (segment.getHoverEvent() != null) {
                segmentComponent = segmentComponent.hoverEvent(segment.getHoverEvent());
            }

            builder.append(segmentComponent);
        }

        return builder.build();
    }

    /**
     * 解析颜色代码
     *
     * @param text 包含&颜色代码的文本
     * @return 解析后的Component
     */
    public static Component parseColorCodes(String text) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .build();
        return serializer.deserialize(text);
    }

    /**
     * 解析可点击消息段
     */
    private static List<MessageSegment> parseClickableSegments(String text) {
        List<MessageSegment> segments = new ArrayList<>();

        // 先解析普通文本和带点击+悬停的内容
        Matcher clickableMatcher = CLICKABLE_PATTERN.matcher(text);
        int lastIndex = 0;

        while (clickableMatcher.find()) {
            if (clickableMatcher.start() > lastIndex) {
                String plainText = text.substring(lastIndex, clickableMatcher.start());
                segments.add(new MessageSegment(plainText));
            }

            String displayText = clickableMatcher.group(1);
            String actionType = clickableMatcher.group(2);
            String actionValue = clickableMatcher.group(3);
            String hoverValue = clickableMatcher.group(4); // 可能为null

            MessageSegment segment = new MessageSegment(displayText);

            // 设置点击事件
            switch (actionType.toLowerCase()) {
                case "run":
                    segment.setClickEvent(ClickEvent.runCommand(actionValue));
                    break;
                case "suggest":
                    segment.setClickEvent(ClickEvent.suggestCommand(actionValue));
                    break;
                case "copy":
                    segment.setClickEvent(ClickEvent.copyToClipboard(actionValue));
                    break;
                case "url":
                    segment.setClickEvent(ClickEvent.openUrl(actionValue));
                    break;
            }

            // 设置悬停事件
            if (hoverValue != null && !hoverValue.isEmpty()) {
                Component hoverText = parseColorCodes(hoverValue);
                segment.setHoverEvent(HoverEvent.showText(hoverText));
            }

            segments.add(segment);
            lastIndex = clickableMatcher.end();
        }

        // 处理剩余的纯文本或纯悬停内容
        String remainingText = lastIndex < text.length() ? text.substring(lastIndex) : "";
        if (!remainingText.isEmpty()) {
            // 检查是否有纯悬停内容
            Matcher hoverMatcher = HOVER_ONLY_PATTERN.matcher(remainingText);
            List<MessageSegment> hoverSegments = new ArrayList<>();
            int hoverLastIndex = 0;

            while (hoverMatcher.find()) {
                if (hoverMatcher.start() > hoverLastIndex) {
                    String plainText = remainingText.substring(hoverLastIndex, hoverMatcher.start());
                    hoverSegments.add(new MessageSegment(plainText));
                }

                String displayText = hoverMatcher.group(1);
                String hoverValue = hoverMatcher.group(3);

                MessageSegment segment = new MessageSegment(displayText);
                Component hoverText = parseColorCodes(hoverValue);
                segment.setHoverEvent(HoverEvent.showText(hoverText));

                hoverSegments.add(segment);
                hoverLastIndex = hoverMatcher.end();
            }

            if (hoverLastIndex < remainingText.length()) {
                String finalText = remainingText.substring(hoverLastIndex);
                hoverSegments.add(new MessageSegment(finalText));
            }

            segments.addAll(hoverSegments);
        }

        return segments;
    }

    // ==================== 内部类 ====================

    private static class MessageSegment {
        private final String text;
        private ClickEvent clickEvent;
        private HoverEvent<?> hoverEvent;

        public MessageSegment(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public ClickEvent getClickEvent() {
            return clickEvent;
        }

        public void setClickEvent(ClickEvent clickEvent) {
            this.clickEvent = clickEvent;
        }

        public HoverEvent<?> getHoverEvent() {
            return hoverEvent;
        }

        public void setHoverEvent(HoverEvent<?> hoverEvent) {
            this.hoverEvent = hoverEvent;
        }
    }

    // ==================== ComponentBuilder 类 ====================

    /**
     * Component构建器 - 提供更直观、类型安全的消息构建方式
     */
    public static class ComponentBuilder {
        private final List<Component> components = new ArrayList<>();

        private ComponentBuilder() {
        }

        /**
         * 创建新的ComponentBuilder实例
         */
        public static ComponentBuilder create() {
            return new ComponentBuilder();
        }

        /**
         * 添加普通文本（支持颜色代码）
         *
         * @param text 文本内容（支持&颜色代码）
         * @return 当前构建器
         */
        public ComponentBuilder text(String text) {
            components.add(parseColorCodes(text));
            return this;
        }

        /**
         * 添加可执行命令的文本
         *
         * @param text      显示文本（支持颜色代码）
         * @param command   命令（不需要/开头也可以）
         * @param hoverText 悬停提示（支持颜色代码，可为null）
         * @return 当前构建器
         */
        public ComponentBuilder runCommand(String text, String command, String hoverText) {
            Component component = parseColorCodes(text)
                    .clickEvent(ClickEvent.runCommand(command.startsWith("/") ? command : "/" + command));

            if (hoverText != null && !hoverText.isEmpty()) {
                component = component.hoverEvent(HoverEvent.showText(parseColorCodes(hoverText)));
            }

            components.add(component);
            return this;
        }

        /**
         * 添加建议命令的文本
         *
         * @param text      显示文本（支持颜色代码）
         * @param command   建议的命令
         * @param hoverText 悬停提示（支持颜色代码，可为null）
         * @return 当前构建器
         */
        public ComponentBuilder suggestCommand(String text, String command, String hoverText) {
            Component component = parseColorCodes(text)
                    .clickEvent(ClickEvent.suggestCommand(command));

            if (hoverText != null && !hoverText.isEmpty()) {
                component = component.hoverEvent(HoverEvent.showText(parseColorCodes(hoverText)));
            }

            components.add(component);
            return this;
        }

        /**
         * 添加复制到剪贴板的文本
         *
         * @param text      显示文本（支持颜色代码）
         * @param copyText  要复制的文本
         * @param hoverText 悬停提示（支持颜色代码，可为null）
         * @return 当前构建器
         */
        public ComponentBuilder copyToClipboard(String text, String copyText, String hoverText) {
            Component component = parseColorCodes(text)
                    .clickEvent(ClickEvent.copyToClipboard(copyText));

            if (hoverText != null && !hoverText.isEmpty()) {
                component = component.hoverEvent(HoverEvent.showText(parseColorCodes(hoverText)));
            }

            components.add(component);
            return this;
        }

        /**
         * 添加打开URL的文本
         *
         * @param text      显示文本（支持颜色代码）
         * @param url       URL地址
         * @param hoverText 悬停提示（支持颜色代码，可为null）
         * @return 当前构建器
         */
        public ComponentBuilder openUrl(String text, String url, String hoverText) {
            Component component = parseColorCodes(text)
                    .clickEvent(ClickEvent.openUrl(url));

            if (hoverText != null && !hoverText.isEmpty()) {
                component = component.hoverEvent(HoverEvent.showText(parseColorCodes(hoverText)));
            }

            components.add(component);
            return this;
        }

        /**
         * 添加带悬停提示的文本（无点击事件）
         *
         * @param text      显示文本（支持颜色代码）
         * @param hoverText 悬停提示（支持颜色代码）
         * @return 当前构建器
         */
        public ComponentBuilder hoverText(String text, String hoverText) {
            Component component = parseColorCodes(text)
                    .hoverEvent(HoverEvent.showText(parseColorCodes(hoverText)));

            components.add(component);
            return this;
        }

        /**
         * 添加任意Component
         *
         * @param component Component对象
         * @return 当前构建器
         */
        public ComponentBuilder append(Component component) {
            if (component != null) {
                components.add(component);
            }
            return this;
        }

        /**
         * 添加换行
         *
         * @return 当前构建器
         */
        public ComponentBuilder newline() {
            components.add(Component.newline());
            return this;
        }

        /**
         * 添加空格
         *
         * @param count 空格数量
         * @return 当前构建器
         */
        public ComponentBuilder space(int count) {
            for (int i = 0; i < count; i++) {
                components.add(Component.space());
            }
            return this;
        }

        /**
         * 构建最终的Component
         *
         * @return 构建完成的Component
         */
        public Component build() {
            if (components.isEmpty()) {
                return Component.empty();
            }

            TextComponent.Builder builder = Component.text();
            for (Component component : components) {
                builder.append(component);
            }
            return builder.build();
        }
    }
}