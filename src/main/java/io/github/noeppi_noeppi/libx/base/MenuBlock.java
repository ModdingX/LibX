package io.github.noeppi_noeppi.libx.base;

import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.libx.menu.BlockEntityMenu;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * This class registers a menu with the Block
 * This also makes the Screen appear on right-click.
 * <p>
 * Note: You need to register the Screen by yourself using the {@link net.minecraft.client.gui.screens.MenuScreens}
 * register function. This can be done on clientSetup or by overriding the initializeClient method on your block.
 *
 * @see net.minecraft.client.gui.screens.MenuScreens
 */
public class MenuBlock<C extends AbstractContainerMenu> extends BlockBase {

    public final MenuType<C> menu;

    public MenuBlock(ModX mod, MenuType<C> menu, Properties properties) {
        super(mod, properties);
        this.menu = menu;
    }

    public MenuBlock(ModX mod, MenuType<C> menu, Properties properties, Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.menu = menu;
    }

    @Override
    public Set<Object> getAdditionalRegisters(ResourceLocation id) {
        return ImmutableSet.builder().addAll(super.getAdditionalRegisters(id)).add(this.menu).build();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide) {
            //noinspection ConstantConditions
            openMenu((ServerPlayer) player, this.menu, new TranslatableComponent("screen." + MenuBlock.this.mod.modid + "." + MenuBlock.this.getRegistryName().getPath()), pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Opens a {@link AbstractContainerMenu} for a player.
     */
    public static void openMenu(ServerPlayer player, MenuType<? extends AbstractContainerMenu> menu, Component title, BlockPos pos) {
        MenuProvider containerProvider = new MenuProvider() {
            @Nonnull
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                buffer.writeBlockPos(pos);
                return menu.create(containerId, inventory, buffer);
            }
        };
        NetworkHooks.openGui(player, containerProvider, pos);
    }
}
