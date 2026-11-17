package dev.sednith.minequestcommandshide;

import java.util.Locale;

public final class CommandName {
    private CommandName() {
    }

    /**
     * Приводит команду к виду, в котором её сравниваем с конфигом:
     * убирает слэш, namespace (plugin:command), аргументы и приводит к нижнему регистру.
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String value = input.trim().toLowerCase(Locale.ROOT);
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        int space = value.indexOf(' ');
        if (space >= 0) {
            value = value.substring(0, space);
        }
        int colon = value.indexOf(':');
        if (colon >= 0) {
            value = value.substring(colon + 1);
        }
        return value.trim();
    }

    public static String firstToken(String command) {
        if (command == null) {
            return "";
        }
        String value = command.trim();
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        int space = value.indexOf(' ');
        return space >= 0 ? value.substring(0, space) : value;
    }

    public static String normalizeGroup(String group) {
        return group == null ? "" : group.trim().toLowerCase(Locale.ROOT);
    }
}
