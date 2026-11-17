package dev.sednith.minequestcommandshide;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandHideConfigTest {
    @Test
    void parsesWhitelistConfig() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: whitelist
                remove-plugin-prefix: true
                groups:
                  default:
                    commands: ['msg', '/tell', 'minecraft:reply']
                  helper:
                    commands:
                      - kick
                """);

        assertEquals(CommandHideConfig.Mode.WHITELIST, config.mode());
        assertTrue(config.removePluginPrefix());
        assertEquals(Set.of("msg", "tell", "reply"), config.commandsForGroup("default"));
        assertEquals(Set.of("kick"), config.commandsForGroup("HELPER"));
        assertEquals(2, config.groupCount());
    }

    @Test
    void parsesBlacklistConfig() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: blacklist
                groups:
                  helper:
                    commands: [vanish]
                """);

        assertEquals(CommandHideConfig.Mode.BLACKLIST, config.mode());
        assertEquals(Set.of("vanish"), config.commandsForGroup("helper"));
    }

    @Test
    void usesDefaultsForMissingSections() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: blacklist
                groups: {}
                """);

        assertTrue(config.help().enabled());
        assertEquals("&e/%s", config.help().format());
        assertEquals(12, config.help().pageSize());
        assertEquals("&fНеизвестная или неполная команда!", config.messages().deny());
    }

    @Test
    void rejectsUnknownMode() {
        InvalidConfigException exception = assertThrows(
                InvalidConfigException.class,
                () -> CommandHideConfig.parse("mode: unknown\ngroups: {}\n")
        );
        assertTrue(exception.getMessage().contains("whitelist"));
    }

    @Test
    void rejectsEmptyWhitelist() {
        assertThrows(
                InvalidConfigException.class,
                () -> CommandHideConfig.parse("mode: whitelist\ngroups: {}\n")
        );
    }

    @Test
    void addsMissingPlaceholderToHelpFormat() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: blacklist
                help:
                  format: '&e'
                groups: {}
                """);

        assertEquals("&e%s", config.help().format());
    }

    @Test
    void clampsPageSize() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: blacklist
                help:
                  page-size: 500
                groups: {}
                """);

        assertEquals(50, config.help().pageSize());
        assertFalse(config.help().header().isBlank());
    }
}
