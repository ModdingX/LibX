package org.moddingx.libx.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.creativetab.CreativeTabX;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;

import java.util.stream.Stream;

/**
 * Base class for {@link Item items} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod.
 */
public class ItemBase extends Item {

    protected final ModX mod;

    public ItemBase(ModX mod, Properties properties) {
        super(properties);
        this.mod = mod;
    }

    /**
     * Returns a {@link Stream} of {@link ItemStack item stacks} to add to a creative tab.
     * {@link CreativeTabX} respects these by default.
     */
    public Stream<ItemStack> makeCreativeTabStacks() {
        return Stream.of(new ItemStack(this));
    }
}