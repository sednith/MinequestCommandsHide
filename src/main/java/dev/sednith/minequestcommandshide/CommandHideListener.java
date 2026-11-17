package dev.sednith.minequestcommandshide;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Слушает события Velocity и применяет правила скрытия команд.
 * Регистрируется вручную из главного класса плагина.
 */
public final class CommandHideListener {
    private final MinequestCommandsHidePlugin plugin;

    public CommandHideListener(MinequestCommandsHidePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Дерево команд, которое прокси отправляет клиенту: отсюда растёт таб-комплит
     * и клиентский список команд.
     */
    @Subscribe
    public void onAvailableCommands(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        CommandHideConfig config = plugin.config();
        CommandRules rules = plugin.rules();
        if (config == null || rules == null) {
            return;
        }

        try {
            if (rules.isBypassed(player)) {
                plugin.visibility().remove(player.getUniqueId());
                return;
            }

            Set<String> groups = rules.groupsOf(player);
            Predicate<String> allowed = name -> rules.isAllowed(groups, player::hasPermission, name);
            Predicate<String> alwaysKeep = name -> isInfrastructure(player, config, name);

            Set<String> visible = CommandTreeFilter.filter(
                    event.getRootNode(),
                    allowed,
                    config.removePluginPrefix(),
                    alwaysKeep
            );
            plugin.visibility().put(player.getUniqueId(), visible);
        } catch (RuntimeException exception) {
            plugin.logger().warn("Не удалось отфильтровать дерево команд для {}", player.getUsername(), exception);
        }
    }

    /**
     * Попытка выполнить скрытую команду: отменяем и присылаем настраиваемый текст.
     * priority = максимальный: обрабатываем раньше остальных плагинов.
     */
    @Subscribe(priority = Short.MAX_VALUE)
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        CommandHideConfig config = plugin.config();
        CommandRules rules = plugin.rules();
        if (config == null || rules == null) {
            return;
        }

        String name = CommandName.normalize(event.getCommand());
        if (name.isEmpty() || rules.isBypassed(player)) {
            return;
        }

        // Свою команду не фильтруем: права проверяет она сама.
        if (isOwnCommand(name)) {
            return;
        }

        if (isHelpCommand(name)) {
            if (!config.help().enabled()) {
                // /help выключен — дальше работает обычная логика режима.
                denyIfHidden(event, player, config, rules, name);
                return;
            }

            event.setResult(CommandExecuteEvent.CommandResult.denied());
            Set<String> commands = plugin.visibility().get(player.getUniqueId())
                    .orElseGet(() -> rules.visibleFallback(player));
            player.sendMessage(HelpRenderer.render(config, commands, pageOf(event.getCommand())));
            return;
        }

        denyIfHidden(event, player, config, rules, name);
    }

    /**
     * Серверные подсказки аргументов: фильтруем только имена команд в корне,
     * аргументы (ник, названия миров и т.п.) не трогаем.
     */
    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        Player player = event.getPlayer();
        CommandHideConfig config = plugin.config();
        CommandRules rules = plugin.rules();
        if (config == null || rules == null || rules.isBypassed(player)) {
            return;
        }

        String partial = event.getPartialMessage();
        if (partial == null) {
            return;
        }
        String trimmed = partial.stripLeading();
        if (!trimmed.startsWith("/")) {
            return;
        }
        String withoutSlash = trimmed.substring(1);
        if (withoutSlash.indexOf(' ') >= 0) {
            return;
        }

        Set<String> groups = rules.groupsOf(player);
        List<String> suggestions = event.getSuggestions();
        suggestions.removeIf(suggestion -> {
            String normalized = CommandName.normalize(suggestion);
            if (normalized.isEmpty() || isInfrastructure(player, config, normalized)) {
                return false;
            }
            return !rules.isAllowed(groups, player::hasPermission, normalized);
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        plugin.visibility().remove(event.getPlayer().getUniqueId());
    }

    private void denyIfHidden(
            CommandExecuteEvent event,
            Player player,
            CommandHideConfig config,
            CommandRules rules,
            String name
    ) {
        Set<String> groups = rules.groupsOf(player);
        if (!rules.isAllowed(groups, player::hasPermission, name)) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            player.sendMessage(Text.legacy(config.messages().deny()));
        }
    }

    private boolean isInfrastructure(Player player, CommandHideConfig config, String name) {
        if (isHelpCommand(name)) {
            return config.help().enabled();
        }
        if (isOwnCommand(name)) {
            return player.hasPermission(CommandRules.RELOAD)
                    || player.hasPermission(CommandRules.SHORT_RELOAD)
                    || player.hasPermission(CommandRules.BYPASS)
                    || player.hasPermission(CommandRules.SHORT_BYPASS);
        }
        return false;
    }

    private static boolean isHelpCommand(String name) {
        return "help".equals(name) || "?".equals(name);
    }

    private static boolean isOwnCommand(String name) {
        return "mch".equals(name) || "minequestcommandshide".equals(name);
    }

    private static int pageOf(String command) {
        if (command == null) {
            return 1;
        }
        String[] parts = command.trim().split("\\s+");
        if (parts.length < 2) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(parts[1]));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}
