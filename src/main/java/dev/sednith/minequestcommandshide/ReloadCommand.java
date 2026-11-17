package dev.sednith.minequestcommandshide;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;

/**
 * /mch reload — перечитывает config.yml без перезапуска прокси.
 */
public final class ReloadCommand implements SimpleCommand {
    private static final String FALLBACK_NO_PERMISSION = "&cНедостаточно прав.";
    private static final String FALLBACK_USAGE = "&eИспользование: /mch reload";
    private static final String FALLBACK_SUCCESS = "&aКонфиг MinequestCommandsHide перезагружен.";
    private static final String FALLBACK_FAILURE = "&cНе удалось загрузить конфиг: %s";

    private final MinequestCommandsHidePlugin plugin;

    public ReloadCommand(MinequestCommandsHidePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        CommandHideConfig config = plugin.config();

        if (!canReload(source)) {
            String message = config == null ? FALLBACK_NO_PERMISSION : config.messages().noPermission();
            source.sendMessage(Text.legacy(message));
            return;
        }

        String[] arguments = invocation.arguments();
        if (arguments.length != 1 || !"reload".equalsIgnoreCase(arguments[0])) {
            String message = config == null ? FALLBACK_USAGE : config.messages().reloadUsage();
            source.sendMessage(Text.legacy(message));
            return;
        }

        String error = plugin.reloadConfig();
        CommandHideConfig current = plugin.config();
        if (error == null) {
            String message = current == null ? FALLBACK_SUCCESS : current.messages().reloadSuccess();
            source.sendMessage(Text.legacy(message));
        } else {
            String template = current == null ? FALLBACK_FAILURE : current.messages().reloadFailure();
            source.sendMessage(Text.legacy(String.format(template, error)));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (!canReload(invocation.source())) {
            return List.of();
        }
        return List.of("reload");
    }

    private boolean canReload(CommandSource source) {
        return source.hasPermission(CommandRules.RELOAD)
                || source.hasPermission(CommandRules.SHORT_RELOAD)
                || source.hasPermission(CommandRules.BYPASS)
                || source.hasPermission(CommandRules.SHORT_BYPASS);
    }
}
