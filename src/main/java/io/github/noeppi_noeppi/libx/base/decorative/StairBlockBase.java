package io.github.noeppi_noeppi.libx.base.decorative;

import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.Registerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Supplier;

public class StairBlockBase extends StairBlock implements Registerable, ChildBlock {

    protected final ModX mod;
    private final Item item;
    private final Block parent;

    public StairBlockBase(ModX mod, Supplier<BlockState> state, Properties properties, Block parent) {
        this(mod, state, properties, new Item.Properties(), parent);
    }

    public StairBlockBase(ModX mod, Supplier<BlockState> state, Properties properties, Item.Properties itemProperties, Block parent) {
        super(state, properties);
        this.mod = mod;
        if (mod.tab != null) {
            itemProperties.tab(mod.tab);
        }
        this.item = new BlockItem(this, itemProperties);
        this.parent = parent;
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return Set.of(this.item);
    }

    @Override
    public Block getParent() {
        return this.parent;
    }
}
