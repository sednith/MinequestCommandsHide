package dev.sednith.minequestcommandshide;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CommandHideConfig {
    public enum Mode {
        WHITELIST,
        BLACKLIST
    }

    public record HelpSettings(
            boolean enabled,
            String header,
            String format,
            String hover,
            String footer,
            String pageFormat,
            int pageSize
    ) {
    }

    public record Messages(
            String deny,
            String noPermission,
            String reloadSuccess,
            String reloadFailure,
            String reloadUsage,
            String helpEmpty
    ) {
    }

    private final Mode mode;
    private final boolean removePluginPrefix;
    private final HelpSettings help;
    private final Messages messages;
    private final Map<String, Set<String>> groups;

    private CommandHideConfig(
            Mode mode,
            boolean removePluginPrefix,
            HelpSettings help,
            Messages messages,
            Map<String, Set<String>> groups
    ) {
        this.mode = mode;
        this.removePluginPrefix = removePluginPrefix;
        this.help = help;
        this.messages = messages;
        this.groups = groups;
    }

    public static CommandHideConfig load(Path path) throws IOException, InvalidConfigException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object value = new Yaml().load(reader);
            if (value == null) {
                throw new InvalidConfigException("config.yml пуст");
            }
            if (!(value instanceof Map<?, ?> map)) {
                throw new InvalidConfigException("корень config.yml должен быть YAML-объектом");
            }
            return fromMap(map);
        } catch (YAMLException exception) {
            throw new InvalidConfigException("ошибка YAML: " + exception.getMessage(), exception);
        }
    }

    static CommandHideConfig parse(String yaml) throws InvalidConfigException {
        Object value = new Yaml().load(yaml);
        if (value == null) {
            throw new InvalidConfigException("config.yml пуст");
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new InvalidConfigException("корень config.yml должен быть YAML-объектом");
        }
        return fromMap(map);
    }

    private static CommandHideConfig fromMap(Map<?, ?> rawRoot) throws InvalidConfigException {
        Map<String, Object> root = stringMap(rawRoot);

        String modeValue = string(root, "mode", "whitelist").trim().toUpperCase(Locale.ROOT);
        Mode mode;
        try {
            mode = Mode.valueOf(modeValue);
        } catch (IllegalArgumentException exception) {
            throw new InvalidConfigException("mode должен быть whitelist или blacklist, получено: " + modeValue);
        }

        Map<String, Object> helpMap = stringMap(asMap(root.get("help")));
        String format = string(helpMap, "format", "&e/%s");
        if (!format.contains("%s")) {
            format = format + "%s";
        }
        HelpSettings help = new HelpSettings(
                bool(helpMap, "enabled", true),
                string(helpMap, "header", "&6Доступные команды:"),
                format,
                string(helpMap, "hover", "&7Нажмите, чтобы выполнить"),
                string(helpMap, "footer", ""),
                string(helpMap, "page-format", "&7Страница {page}/{pages}"),
                clamp(integer(helpMap, "page-size", 12), 1, 50)
        );

        Map<String, Object> messagesMap = stringMap(asMap(root.get("messages")));
        Messages messages = new Messages(
                string(messagesMap, "deny", "&fНеизвестная или неполная команда!"),
                string(messagesMap, "no-permission", "&cНедостаточно прав."),
                string(messagesMap, "reload-success", "&aКонфиг MinequestCommandsHide перезагружен."),
                string(messagesMap, "reload-failure", "&cНе удалось загрузить конфиг: %s"),
                string(messagesMap, "reload-usage", "&eИспользование: /mch reload"),
                string(messagesMap, "help-empty", "&7Нет доступных команд.")
        );

        Map<String, Set<String>> groups = new LinkedHashMap<>();
        Map<String, Object> groupsMap = stringMap(asMap(root.get("groups")));
        for (Map.Entry<String, Object> entry : groupsMap.entrySet()) {
            String name = CommandName.normalizeGroup(entry.getKey());
            if (name.isEmpty()) {
                continue;
            }

            Map<String, Object> groupMap = stringMap(asMap(entry.getValue()));
            Set<String> commands = new LinkedHashSet<>();
            Object commandsValue = groupMap.get("commands");
            if (commandsValue instanceof List<?> list) {
                for (Object item : list) {
                    String command = CommandName.normalize(String.valueOf(item));
                    if (!command.isEmpty()) {
                        commands.add(command);
                    }
                }
            }
            groups.put(name, Collections.unmodifiableSet(commands));
        }

        if (mode == Mode.WHITELIST && groups.isEmpty()) {
            throw new InvalidConfigException("в режиме whitelist нужна хотя бы одна группа с командами");
        }

        return new CommandHideConfig(
                mode,
                bool(root, "remove-plugin-prefix", true),
                help,
                messages,
                Collections.unmodifiableMap(groups)
        );
    }

    public Mode mode() {
        return mode;
    }

    public boolean removePluginPrefix() {
        return removePluginPrefix;
    }

    public HelpSettings help() {
        return help;
    }

    public Messages messages() {
        return messages;
    }

    public Set<String> commandsForGroup(String group) {
        return groups.getOrDefault(CommandName.normalizeGroup(group), Set.of());
    }

    public boolean hasGroup(String group) {
        return groups.containsKey(CommandName.normalizeGroup(group));
    }

    public int groupCount() {
        return groups.size();
    }

    public Map<String, Set<String>> groups() {
        return groups;
    }

    private static Map<String, Object> stringMap(Map<?, ?> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    private static Map<?, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }

    private static String string(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private static boolean bool(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? defaultValue : Boolean.parseBoolean(String.valueOf(value));
    }

    private static int integer(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
