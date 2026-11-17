package dev.sednith.minequestcommandshide;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Формирует ответ на /help только из тех команд, которые игрок реально видит.
 */
public final class HelpRenderer {
    private static final Set<String> TECHNICAL = Set.of("help", "?", "mch", "minequestcommandshide");

    private HelpRenderer() {
    }

    public static Set<String> displayable(Collection<String> commands) {
        Set<String> result = new LinkedHashSet<>();
        for (String command : commands) {
            String normalized = CommandName.normalize(command);
            if (!normalized.isEmpty() && !TECHNICAL.contains(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    public static Component render(CommandHideConfig config, Collection<String> commands, int requestedPage) {
        CommandHideConfig.HelpSettings help = config.help();
        List<String> names = new ArrayList<>(displayable(commands));
        names.sort(String.CASE_INSENSITIVE_ORDER);

        if (names.isEmpty()) {
            return Text.legacy(config.messages().helpEmpty());
        }

        int pageSize = Math.max(1, help.pageSize());
        int pages = (names.size() + pageSize - 1) / pageSize;
        int page = Math.max(1, Math.min(pages, requestedPage));

        Component result = Text.legacy(help.header());
        int from = (page - 1) * pageSize;
        int to = Math.min(names.size(), from + pageSize);
        for (int i = from; i < to; i++) {
            String name = names.get(i);
            Component line = Text.legacy(String.format(help.format(), name))
                    .clickEvent(ClickEvent.runCommand("/" + name))
                    .hoverEvent(HoverEvent.showText(Text.legacy(help.hover())));
            result = result.append(Component.newline()).append(line);
        }

        if (pages > 1) {
            String pageLine = help.pageFormat()
                    .replace("{page}", Integer.toString(page))
                    .replace("{pages}", Integer.toString(pages));
            result = result.append(Component.newline()).append(Text.legacy(pageLine));
        }

        if (help.footer() != null && !help.footer().isBlank()) {
            result = result.append(Component.newline()).append(Text.legacy(help.footer()));
        }
        return result;
    }
}
