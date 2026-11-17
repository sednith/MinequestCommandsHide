package dev.sednith.minequestcommandshide;

import com.velocitypowered.api.proxy.Player;

import java.util.Set;

public interface GroupResolver {
    Set<String> groupsOf(Player player);
}
