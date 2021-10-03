package io.github.noeppi_noeppi.libx.base.decorative;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Supplier;

public class StairBlockBase extends StairBlock implements Registerable {

    protected final ModX mod;
    private final Item item;

    public StairBlockBase(ModX mod, Supplier<BlockState> state, Properties properties) {
        this(mod, state, properties, new Item.Properties());
    }

    public StairBlockBase(ModX mod, Supplier<BlockState> state, Properties properties, Item.Properties itemProperties) {
        super(state, properties);
        this.mod = mod;
        if (mod.tab != null) {
            itemProperties.tab(mod.tab);
        }
        this.item = new BlockItem(this, itemProperties);
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Set.of(this.item);
    }
}
