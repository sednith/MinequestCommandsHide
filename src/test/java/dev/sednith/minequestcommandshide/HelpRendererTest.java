package dev.sednith.minequestcommandshide;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpRendererTest {
    private static String plain(net.kyori.adventure.text.Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static CommandHideConfig config(String extraHelp) throws Exception {
        return CommandHideConfig.parse(
                "mode: whitelist\n"
                        + "help:\n"
                        + extraHelp
                        + "groups:\n"
                        + "  default:\n"
                        + "    commands: [msg]\n"
        );
    }

    @Test
    void rendersOnlyVisibleCommands() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: whitelist
                groups:
                  default:
                    commands: [msg]
                """);

        String text = plain(HelpRenderer.render(config, List.of("msg", "help", "mch"), 1));

        assertTrue(text.contains("/msg"));
        assertFalse(text.contains("/help"));
        assertFalse(text.contains("/mch"));
    }

    @Test
    void showsEmptyMessageWhenNothingVisible() throws Exception {
        CommandHideConfig config = CommandHideConfig.parse("""
                mode: whitelist
                groups:
                  default:
                    commands: [msg]
                """);

        String text = plain(HelpRenderer.render(config, List.of("help", "mch"), 1));

        assertEquals("Нет доступных команд.", text);
    }

    @Test
    void paginatesAndClampsPages() throws Exception {
        CommandHideConfig config = config("  page-size: 2\n  page-format: '&7Страница {page}/{pages}'\n");

        String first = plain(HelpRenderer.render(config, List.of("alpha", "beta", "gamma"), 1));
        String second = plain(HelpRenderer.render(config, List.of("alpha", "beta", "gamma"), 2));
        String tooFar = plain(HelpRenderer.render(config, List.of("alpha", "beta", "gamma"), 99));

        assertTrue(first.contains("/alpha") && first.contains("/beta") && !first.contains("/gamma"));
        assertTrue(second.contains("/gamma") && !second.contains("/alpha"));
        assertTrue(second.contains("Страница 2/2"));
        assertTrue(tooFar.contains("/gamma"));
    }
}
