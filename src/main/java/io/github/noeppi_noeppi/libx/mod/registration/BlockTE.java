package io.github.noeppi_noeppi.libx.mod.registration;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Set;

public class BlockTE<T extends TileEntityBase> extends BlockBase {

    private final Class<T> teClass;
    private final Constructor<T> teCtor;
    private final TileEntityType<T> teType;

    public BlockTE(ModX mod, Class<T> teClass, Properties properties) {
        this(mod, teClass, properties, new Item.Properties());
    }

    public BlockTE(ModX mod, Class<T> teClass, Properties properties, Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.teClass = teClass;

        try {
            this.teCtor = teClass.getConstructor(TileEntityType.class);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            throw new RuntimeException("Could not get constructor for tile entity " + teClass + ".", e);
        }
        //noinspection ConstantConditions
        this.teType = new TileEntityType<>(() -> {
            try {
                return this.teCtor.newInstance(this.getTileType());
            } catch (ReflectiveOperationException e) {
                if (e.getCause() != null)
                    e.getCause().printStackTrace();
                throw new RuntimeException("Could not create TileEntity of type " + teClass + ".", e);
            }
        }, ImmutableSet.of(this), null);
    }

    @Override
    public Set<Object> getAdditionalRegisters() {
        return ImmutableSet.builder().addAll(super.getAdditionalRegisters()).add(teType).build();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public T createTileEntity(BlockState state, IBlockReader world) {
        return this.teType.create();
    }

    public T getTile(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null || !this.teClass.isAssignableFrom(te.getClass())) {
            throw new IllegalStateException("Expected a tile entity of type " + teClass + " at " + world + " " + pos + ", got" + te);
        }
        //noinspection unchecked
        return (T) te;
    }

    public TileEntityType<T> getTileType() {
        return this.teType;
    }
}
