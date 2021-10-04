package io.github.noeppi_noeppi.libx.base.decorative;

import io.github.noeppi_noeppi.libx.base.BlockBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for {@link net.minecraft.world.level.block.Block blocks} for mods using {@link ModXRegistration}. This
 * will automatically set the creative tab if it's defined in the mod and a register a
 * {@link net.minecraft.world.item.BlockItem block item}. Additionally, it will register a slab and stairs
 * {@link net.minecraft.world.level.block.Block block}, and based on the {@link Type block type} it will also generate
 * a wall, or fence and fence gate. Everything again with the {@link net.minecraft.world.item.BlockItem block item}.
 */
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
        this.slab = new SlabBlockBase(mod, properties, itemProperties, this);
        this.stairs = new StairBlockBase(mod, this::defaultBlockState, properties, itemProperties, this);

        if (type == Type.WALL) {
            this.wall = new WallBlockBase(mod, properties, itemProperties, this);
        } else {
            this.wall = null;
        }

        if (type == Type.FENCE) {
            this.fence = new FenceBlockBase(mod, properties, itemProperties, this);
            this.fenceGate = new FenceGateBlockBase(mod, properties, itemProperties, this);
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

    public SlabBlockBase getSlab() {
        return this.slab;
    }

    public StairBlockBase getStairs() {
        return this.stairs;
    }

    @Nullable
    public WallBlockBase getWall() {
        return this.wall;
    }

    @Nullable
    public FenceBlockBase getFence() {
        return this.fence;
    }

    @Nullable
    public FenceGateBlockBase getFenceGate() {
        return this.fenceGate;
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
