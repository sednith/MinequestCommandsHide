package dev.sednith.minequestcommandshide;

import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LuckPermsGroupResolver implements GroupResolver {
    private final Logger logger;
    private final AtomicBoolean warningLogged = new AtomicBoolean();

    public LuckPermsGroupResolver(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<String> groupsOf(Player player) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return Set.of();
            }

            Set<String> groups = new LinkedHashSet<>();
            String primary = user.getPrimaryGroup();
            if (primary != null && !primary.isBlank()) {
                groups.add(primary.toLowerCase(Locale.ROOT));
            }
            for (Group group : user.getInheritedGroups(QueryOptions.nonContextual())) {
                groups.add(group.getName().toLowerCase(Locale.ROOT));
            }
            return Set.copyOf(groups);
        } catch (Throwable throwable) {
            if (warningLogged.compareAndSet(false, true)) {
                logger.warn("Не удалось получить группы LuckPerms, используется только группа default", throwable);
            }
            return Set.of();
        }
    }
}
