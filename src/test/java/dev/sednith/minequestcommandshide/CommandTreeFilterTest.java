package dev.sednith.minequestcommandshide;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandTreeFilterTest {
    private static RootCommandNode<Object> tree(String... names) {
        RootCommandNode<Object> root = new RootCommandNode<>();
        for (String name : names) {
            root.addChild(LiteralArgumentBuilder.literal(name).build());
        }
        return root;
    }

    private static Set<String> childNames(RootCommandNode<Object> root) {
        Set<String> names = new java.util.LinkedHashSet<>();
        for (CommandNode<Object> node : root.getChildren()) {
            names.add(node.getName());
        }
        return names;
    }

    @Test
    void removesHiddenCommands() {
        RootCommandNode<Object> root = tree("msg", "kick", "ban");

        Set<String> visible = CommandTreeFilter.filter(
                root,
                name -> Set.of("msg").contains(name),
                true,
                name -> false
        );

        assertEquals(Set.of("msg"), visible);
        assertEquals(Set.of("msg"), childNames(root));
    }

    @Test
    void keepsAlwaysKeptCommands() {
        RootCommandNode<Object> root = tree("msg", "help");

        Set<String> visible = CommandTreeFilter.filter(
                root,
                name -> "msg".equals(name),
                true,
                name -> "help".equals(name)
        );

        assertTrue(visible.contains("help"));
        assertTrue(childNames(root).contains("help"));
    }

    @Test
    void removesNamespaceEntriesWhenPrefixDisabled() {
        RootCommandNode<Object> root = tree("msg", "minecraft:tp");

        Set<String> visible = CommandTreeFilter.filter(
                root,
                name -> Set.of("msg", "tp").contains(name),
                true,
                name -> false
        );

        assertEquals(Set.of("msg"), visible);
        assertFalse(childNames(root).contains("minecraft:tp"));
    }

    @Test
    void keepsNamespaceEntriesWhenPrefixEnabled() {
        RootCommandNode<Object> root = tree("msg", "minecraft:tp");

        Set<String> visible = CommandTreeFilter.filter(
                root,
                name -> Set.of("msg", "tp").contains(name),
                false,
                name -> false
        );

        assertEquals(Set.of("msg", "tp"), visible);
        assertTrue(childNames(root).contains("minecraft:tp"));
    }

    @Test
    void filtersSuggestionsInPlace() {
        List<String> suggestions = new ArrayList<>(List.of("msg", "kick", "ban"));

        CommandTreeFilter.filterSuggestions(suggestions, name -> "msg".equals(name));

        assertEquals(List.of("msg"), suggestions);
    }
}
