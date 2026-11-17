package dev.sednith.minequestcommandshide;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class CommandTreeFilter {
    private CommandTreeFilter() {
    }

    /**
     * Удаляет из дерева команд недоступные узлы и возвращает нормализованные имена видимых команд.
     */
    public static Set<String> filter(
            RootCommandNode<?> root,
            Predicate<String> allowed,
            boolean removePluginPrefix,
            Predicate<String> alwaysKeep
    ) {
        Set<String> visible = new LinkedHashSet<>();
        for (CommandNode<?> node : new ArrayList<>(root.getChildren())) {
            String rawName = node.getName();
            String normalized = CommandName.normalize(rawName);

            if (removePluginPrefix && rawName.indexOf(':') >= 0) {
                root.getChildren().remove(node);
                continue;
            }

            if (alwaysKeep.test(normalized) || allowed.test(normalized)) {
                if (!normalized.isEmpty()) {
                    visible.add(normalized);
                }
            } else {
                root.getChildren().remove(node);
            }
        }
        return visible;
    }

    public static void filterSuggestions(List<String> suggestions, Predicate<String> allowed) {
        suggestions.removeIf(suggestion -> {
            String normalized = CommandName.normalize(suggestion);
            return !normalized.isEmpty() && !allowed.test(normalized);
        });
    }
}
