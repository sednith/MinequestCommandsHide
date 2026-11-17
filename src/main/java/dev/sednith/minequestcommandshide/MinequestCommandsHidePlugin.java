package dev.sednith.minequestcommandshide;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Plugin(
        id = "minequestcommandshide",
        name = "MinequestCommandsHide",
        version = "1.0.0",
        description = "Скрывает команды от игроков по группам LuckPerms",
        url = "https://github.com/sednith/MinequestCommandsHide",
        authors = {"sednith"},
        dependencies = {@Dependency(id = "luckperms", optional = true)}
)
public final class MinequestCommandsHidePlugin {
    private static final String CONFIG_RESOURCE = "config.yml";
    private static final String CONFIG_FILE = "config.yml";

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private final GroupResolver groupResolver;
    private final VisibilityCache visibility = new VisibilityCache();

    private volatile CommandHideConfig config;
    private volatile CommandRules rules;

    @Inject
    public MinequestCommandsHidePlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.groupResolver = new LuckPermsGroupResolver(logger);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        ensureConfigFile();
        reloadConfig();
        registerCommand();
        server.getEventManager().register(this, new CommandHideListener(this));
    }

    public CommandHideConfig config() {
        return config;
    }

    public CommandRules rules() {
        return rules;
    }

    public VisibilityCache visibility() {
        return visibility;
    }

    public Logger logger() {
        return logger;
    }

    public Path configPath() {
        return dataDirectory.resolve(CONFIG_FILE);
    }

    /**
     * Перечитывает конфиг. Возвращает null при успехе или текст ошибки.
     * При ошибке старый рабочий конфиг остаётся в силе (fail-open на старте).
     */
    public synchronized String reloadConfig() {
        try {
            CommandHideConfig loaded = CommandHideConfig.load(configPath());
            this.config = loaded;
            this.rules = new CommandRules(loaded, groupResolver);
            this.visibility.clear();
            logger.info(
                    "Конфиг MinequestCommandsHide загружен: режим {}, групп {}",
                    loaded.mode().name().toLowerCase(),
                    loaded.groupCount()
            );
            return null;
        } catch (IOException | InvalidConfigException exception) {
            logger.error("Не удалось загрузить конфиг {}: {}", configPath(), exception.getMessage(), exception);
            return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        }
    }

    private void registerCommand() {
        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("mch")
                .aliases("minequestcommandshide")
                .plugin(this)
                .build();
        commandManager.register(meta, new ReloadCommand(this));
    }

    private void ensureConfigFile() {
        Path path = configPath();
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(CONFIG_RESOURCE)) {
                if (stream == null) {
                    logger.error("В jar плагина не найден {}", CONFIG_RESOURCE);
                    return;
                }
                Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Создан конфиг по умолчанию: {}", path);
            }
        } catch (IOException exception) {
            logger.error("Не удалось создать конфиг {}", path, exception);
        }
    }
}
