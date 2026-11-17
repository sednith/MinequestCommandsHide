package dev.sednith.minequestcommandshide;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Text() {
    }

    public static Component legacy(String message) {
        return LEGACY.deserialize(message == null ? "" : message);
    }
}
