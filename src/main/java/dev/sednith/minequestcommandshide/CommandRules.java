package dev.sednith.minequestcommandshide;

import com.velocitypowered.api.proxy.Player;

import java.util.LinkedHashSet;
import java.util.Set;

public final class CommandRules {
    public static final String BYPASS = "minequestcommandshide.bypass";
    public static final String SHORT_BYPASS = "mch.bypass";
    public static final String RELOAD = "minequestcommandshide.reload";
    public static final String SHORT_RELOAD = "mch.reload";

    private final CommandHideConfig config;
    private final GroupResolver groupResolver;

    public CommandRules(CommandHideConfig config, GroupResolver groupResolver) {
        this.config = config;
        this.groupResolver = groupResolver;
    }

    public Set<String> groupsOf(Player player) {
        Set<String> groups = new LinkedHashSet<>(groupResolver.groupsOf(player));
        groups.add("default");
        return groups;
    }

    public Set<String> commandsFor(Set<String> groups) {
        Set<String> commands = new LinkedHashSet<>();
        for (String group : groups) {
            commands.addAll(config.commandsForGroup(group));
        }
        return commands;
    }

    public Set<String> commandsFor(Player player) {
        return commandsFor(groupsOf(player));
    }

    public boolean isBypassed(PermissionCheck permissions) {
        return permissions.has(BYPASS) || permissions.has(SHORT_BYPASS);
    }

    public boolean isBypassed(Player player) {
        return isBypassed(player::hasPermission);
    }

    public boolean isAllowed(Set<String> groups, PermissionCheck permissions, String command) {
        if (isBypassed(permissions)) {
            return true;
        }

        String normalized = CommandName.normalize(command);
        if (normalized.isEmpty()) {
            return false;
        }

        boolean listed = commandsFor(groups).contains(normalized);
        return config.mode() == CommandHideConfig.Mode.WHITELIST ? listed : !listed;
    }

    public boolean isAllowed(Player player, String command) {
        return isAllowed(groupsOf(player), player::hasPermission, command);
    }

    public Set<String> visibleFallback(Player player) {
        return config.mode() == CommandHideConfig.Mode.WHITELIST ? commandsFor(player) : Set.of();
    }

    @FunctionalInterface
    public interface PermissionCheck {
        boolean has(String permission);
    }
}
