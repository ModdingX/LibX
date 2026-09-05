package org.moddingx.libx.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.moddingx.libx.creativetab.CreativeTabItemProvider;
import org.moddingx.libx.creativetab.CreativeTabX;

import java.util.stream.Stream;

public class SpawnEggItemBase extends SpawnEggItem implements CreativeTabItemProvider {

    private final int primaryColor;
    private final int secondaryColor;

    public SpawnEggItemBase(EntityType<? extends Mob> defaultType, int primaryColor, int secondaryColor, Item.Properties properties) {
        super(properties.spawnEgg(defaultType));
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public int getPrimaryColor() {
        return this.primaryColor;
    }

    public int getSecondaryColor() {
        return this.secondaryColor;
    }

    @Override
    public Stream<ItemStack> makeCreativeTabStacks(CreativeTabX.TabContext ctx) {
        return Stream.of(new ItemStack(this));
    }
}
