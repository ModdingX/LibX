package org.moddingx.libx.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.creativetab.CreativeTabItemProvider;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;

import java.util.stream.Stream;

/**
 * Base class for {@link Item items} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod.
 */
public class ItemBase extends Item implements CreativeTabItemProvider {

    protected final ModX mod;

    public ItemBase(ModX mod, Properties properties) {
        super(properties);
        this.mod = mod;
    }

    @Override
    public Stream<ItemStack> makeCreativeTabStacks() {
        return Stream.of(new ItemStack(this));
    }
}
