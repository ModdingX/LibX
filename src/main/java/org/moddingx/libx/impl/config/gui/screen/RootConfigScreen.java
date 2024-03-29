package org.moddingx.libx.impl.config.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.moddingx.libx.impl.config.ConfigImpl;
import org.moddingx.libx.impl.config.ConfigKey;

import javax.annotation.Nullable;
import java.util.*;

public class RootConfigScreen extends ConfigScreen<ConfigKey> {

    public RootConfigScreen(ConfigScreenManager manager, ConfigImpl config) {
        super(manager, Component.translatable("libx.config.gui.config.title", config.id.getPath()), buildGrouped(config), RootConfigScreen::createEntry, RootConfigScreen::search);
    }

    private static Map<BuiltCategory, List<ConfigKey>> buildGrouped(ConfigImpl config) {
        Map<String, List<ConfigKey>> map = new HashMap<>();
        Map<String, BuiltCategory> categories = new HashMap<>();
        for (ConfigKey key : config.keys.values()) {
            if (key.path.size() == 1) {
                categories.putIfAbsent("", BuiltCategory.EMPTY);
                map.computeIfAbsent("", k -> new ArrayList<>()).add(key);
            } else {
                String categoryId = String.join(".", key.path.subList(0, key.path.size() - 1));
                categories.computeIfAbsent(categoryId, k -> config.groups.stream()
                        .filter(group -> categoryId.equals(String.join(".", group.path)))
                        .findFirst().map(group -> new BuiltCategory(
                                String.join(".", group.path),
                                Component.literal(String.join(".", group.path)),
                                group.comment.stream().map(Component::literal).collect(ImmutableList.toImmutableList())
                        )).orElseGet(() -> new BuiltCategory(
                                String.join(".", key.path.subList(0, key.path.size() - 1)),
                                Component.literal(String.join(".", key.path.subList(0, key.path.size() - 1))),
                                List.of()
                        )));
                map.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(key);
            }
        }
        ImmutableMap.Builder<BuiltCategory, List<ConfigKey>> builder = ImmutableMap.builder();
        for (Map.Entry<String, List<ConfigKey>> entry : map.entrySet()) {
            builder.put(categories.get(entry.getKey()), ImmutableList.copyOf(entry.getValue()));
        }
        return builder.build();
    }

    private static BuiltEntry createEntry(ConfigKey key, ConfigScreen<ConfigKey> screen, @Nullable AbstractWidget old, int x, int y, int width, int height) {
        return new BuiltEntry(
                Component.literal(key.path.get(key.path.size() - 1)),
                key.comment.stream().map(Component::literal).collect(ImmutableList.toImmutableList()),
                screen.display.createWidget(key, screen, old, x, y, width, height)
        );
    }

    private static boolean search(ConfigKey key, ConfigScreen<ConfigKey> screen, String query) {
        return query.isBlank() || String.join(".", key.path).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                || key.comment.stream().anyMatch(str -> str.toLowerCase(Locale.ROOT).contains(query));
    }
}
