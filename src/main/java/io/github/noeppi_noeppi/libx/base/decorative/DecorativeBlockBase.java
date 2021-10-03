package io.github.noeppi_noeppi.libx.base.decorative;

import io.github.noeppi_noeppi.libx.base.BlockBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class DecorativeBlockBase extends BlockBase {

    private final Type type;
    private final SlabBlockBase slab;
    private final StairBlockBase stairs;
    private final WallBlockBase wall;
    private final FenceBlockBase fence;
    private final FenceGateBlockBase fenceGate;

    public DecorativeBlockBase(ModX mod, Properties properties, Type type) {
        this(mod, properties, new Item.Properties(), type);
    }

    public DecorativeBlockBase(ModX mod, Properties properties, Item.Properties itemProperties, Type type) {
        super(mod, properties, itemProperties);
        this.type = type;

        this.slab = new SlabBlockBase(mod, properties, itemProperties);

        this.stairs = new StairBlockBase(mod, this::defaultBlockState, properties, itemProperties);

        if (type == Type.WALL) {
            this.wall = new WallBlockBase(mod, properties, itemProperties);
        } else {
            this.wall = null;
        }

        if (type == Type.FENCE) {
            this.fence = new FenceBlockBase(mod, properties, itemProperties);
            this.fenceGate = new FenceGateBlockBase(mod, properties, itemProperties);
        } else {
            this.fence = null;
            this.fenceGate = null;
        }
    }

    public Type getDecoType() {
        return this.type;
    }

    public boolean hasFence() {
        return this.type == Type.FENCE;
    }

    public boolean hasWall() {
        return this.type == Type.WALL;
    }

    @Override
    public Map<String, Object> getNamedAdditionalRegisters(ResourceLocation id) {
        Map<String, Object> registrable = new HashMap<>();
        registrable.put("slab", this.slab);
        registrable.put("stairs", this.stairs);
        if (this.type == Type.WALL) {
            registrable.put("wall", this.wall);
        }
        if (this.type == Type.FENCE) {
            registrable.put("fence", this.fence);
            registrable.put("fence_gate", this.fenceGate);
        }
        return registrable;
    }

    public enum Type {
        FENCE,
        WALL
    }
}
