package dev.sednith.minequestcommandshide;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кэш последнего отфильтрованного дерева команд для каждого игрока.
 * Нужен, чтобы /help показывал ровно то, что игрок видит в таб-комплите.
 */
public final class VisibilityCache {
    private final Map<UUID, Set<String>> visible = new ConcurrentHashMap<>();

    public void put(UUID playerId, Set<String> commands) {
        visible.put(playerId, Set.copyOf(commands));
    }

    public Optional<Set<String>> get(UUID playerId) {
        return Optional.ofNullable(visible.get(playerId));
    }

    public void remove(UUID playerId) {
        visible.remove(playerId);
    }

    public void clear() {
        visible.clear();
    }
}
