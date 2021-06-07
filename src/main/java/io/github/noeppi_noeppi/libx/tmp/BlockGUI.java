package io.github.noeppi_noeppi.libx.tmp;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.inventory.container.ContainerBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * This block registers a container to it'S tile entity and handles the gui opening when the
 * block is right clicked. You still need to manually register the screen on the client.
 */
public class BlockGUI<T extends TileEntity, C extends ContainerBase<T>> extends BlockTE<T> {

    public final ContainerType<C> container;

    public BlockGUI(ModX mod, Class<T> teClass, ContainerType<C> container, Properties properties) {
        super(mod, teClass, properties);
        this.container = container;
    }

    public BlockGUI(ModX mod, Class<T> teClass, ContainerType<C> container, Properties properties, Item.Properties itemProperties) {
        super(mod, teClass, properties, itemProperties);
        this.container = container;
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return ImmutableSet.builder().addAll(super.getAdditionalRegisters(id)).add(this.container).build();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")

    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult hit) {
        if (!world.isRemote) {
            INamedContainerProvider containerProvider = new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    //noinspection ConstantConditions
                    return new TranslationTextComponent("screen." + BlockGUI.this.mod.modid + "." + BlockGUI.this.getRegistryName().getPath());
                }

                @Override
                public Container createMenu(int windowId, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity player) {
                    PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                    buffer.writeBlockPos(pos);
                    return BlockGUI.this.container.create(windowId, playerInventory, buffer);
                }
            };
            NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, pos);
        }
        return ActionResultType.SUCCESS;
    }
}
