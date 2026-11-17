package dev.sednith.minequestcommandshide;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRulesTest {
    private static CommandRules rules(String yaml) throws Exception {
        CommandHideConfig config = CommandHideConfig.parse(yaml);
        return new CommandRules(config, player -> Set.of());
    }

    @Test
    void mergesDefaultAndPlayerGroups() throws Exception {
        CommandRules rules = rules("""
                mode: whitelist
                groups:
                  default:
                    commands: [msg, list]
                  helper:
                    commands: [kick]
                """);

        assertEquals(Set.of("msg", "list", "kick"), rules.commandsFor(Set.of("default", "helper")));
    }

    @Test
    void whitelistAllowsOnlyListedCommands() throws Exception {
        CommandRules rules = rules("""
                mode: whitelist
                groups:
                  default:
                    commands: [msg]
                """);

        assertTrue(rules.isAllowed(Set.of("default"), permission -> false, "msg"));
        assertTrue(rules.isAllowed(Set.of("default"), permission -> false, "/minecraft:MSG Steve"));
        assertFalse(rules.isAllowed(Set.of("default"), permission -> false, "kick"));
        assertFalse(rules.isAllowed(Set.of("default"), permission -> false, ""));
    }

    @Test
    void blacklistAllowsEverythingExceptListedCommands() throws Exception {
        CommandRules rules = rules("""
                mode: blacklist
                groups:
                  default:
                    commands: [kick, ban]
                """);

        assertFalse(rules.isAllowed(Set.of("default"), permission -> false, "kick"));
        assertTrue(rules.isAllowed(Set.of("default"), permission -> false, "msg"));
    }

    @Test
    void bypassAllowsEverything() throws Exception {
        CommandRules rules = rules("""
                mode: whitelist
                groups:
                  default:
                    commands: [msg]
                """);

        assertTrue(rules.isAllowed(Set.of("default"), permission -> true, "kick"));
        assertFalse(rules.isBypassed(permission -> false));
        assertTrue(rules.isBypassed(permission -> permission.equals(CommandRules.SHORT_BYPASS)));
    }

    @Test
    void blacklistFallbackIsEmpty() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: blacklist
                groups: {}
                """);
        CommandRules blacklist = new CommandRules(config, player -> Set.of());
        assertTrue(blacklist.visibleFallback(null).isEmpty());
    }
}
