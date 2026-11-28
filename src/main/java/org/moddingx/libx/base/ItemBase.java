package org.moddingx.libx.base;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import org.moddingx.libx.creativetab.CreativeTabItemProvider;
import org.moddingx.libx.creativetab.CreativeTabX;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.util.game.CookingFuels;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.stream.Stream;

/**
 * Base class for {@link Item items} for mods using {@link ModXRegistration}. This will automatically set the
 * creative tab if it's defined in the mod.
 */
public class ItemBase extends Item implements Registerable, CreativeTabItemProvider {

    protected final ModX mod;

    public ItemBase(ModX mod, Properties properties) {
        super(properties);
        this.mod = mod;
    }

    public int burnTime() {
        return 0;
    }

    @Override
    public Stream<ItemStack> makeCreativeTabStacks(CreativeTabX.TabContext ctx) {
        return Stream.of(new ItemStack(this));
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        int burnTime = this.burnTime();
        if (burnTime > 0) {
            ResourceKey<ContextIntProvider> fuel = CookingFuels.burnTime(this.mod, burnTime);
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(ResourceKey.create(Registries.ITEM, ctx.id()),
                    (components, lookup, key) -> components.set(DataComponents.COOKING_FUEL,
                            new CookingFuel(fuel, ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER)));
        }
    }
}
