package org.moddingx.libx.impl.libxcore;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.moddingx.libx.impl.datagen.load.DatagenResourceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreTagLoad {

    /**
     * Patched into {@link TagLoader#loadTagsForRegistry(ResourceManager, net.minecraft.resources.ResourceKey, TagLoader.ElementLookup)}
     * after the call to {@link TagLoader#load(ResourceManager)}.
     *
     * During a LibX datagen registry bootstrap, every entry of the tags belonging to the registry that is currently
     * being constructed is made optional. Required entries are resolved through a lookup that mints an unbound
     * registration holder for elements which are not there, and elements that a {@code RegistryProvider} creates are
     * not part of the datagen input, so freezing the registry would fail. As all elements of a registry are registered
     * before its tags are loaded, an entry that can be resolved resolves through the read-only lookup just as well,
     * while an entry that can not is skipped instead of failing the whole tag. Tags that were created while decoding
     * registry elements are therefore still bound.
     *
     * Outside of that bootstrap this is a no-op.
     */
    public static Map<Identifier, List<TagLoader.EntryWithSource>> processRegistryTagEntries(Map<Identifier, List<TagLoader.EntryWithSource>> entries, ResourceManager manager) {
        if (!DatagenResourceManager.isRegistryBootstrap(manager)) return entries;
        Map<Identifier, List<TagLoader.EntryWithSource>> result = new HashMap<>();
        entries.forEach((id, tagEntries) -> result.put(id, tagEntries.stream().map(CoreTagLoad::asOptional).toList()));
        return result;
    }

    private static TagLoader.EntryWithSource asOptional(TagLoader.EntryWithSource entry) {
        if (!entry.entry().isRequired()) return entry;
        Identifier id = entry.entry().getId();
        TagEntry optional = entry.entry().isTag() ? TagEntry.optionalTag(id) : TagEntry.optionalElement(id);
        return new TagLoader.EntryWithSource(optional, entry.source(), entry.remove());
    }
}
