package io.github.noeppi_noeppi.libx.registration.base;

import io.github.noeppi_noeppi.libx.menu.BlockMenu;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.registration.RegistrationContext;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Objects;

/**
 * This class registers a menu with the Block.
 * This also makes the Screen appear on right-click.
 * <p>
 * Note: You need to register the Screen by yourself using the {@link MenuScreens} register function.
 *
 * @see BlockMenu
 */
public class MenuBlock<C extends BlockMenu> extends BlockBase {

    public final MenuType<C> menu;

    public MenuBlock(ModX mod, MenuType<C> menu, Properties properties) {
        this(mod, menu, properties, new Item.Properties());
    }

    public MenuBlock(ModX mod, MenuType<C> menu, Properties properties, @Nullable Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.menu = menu;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void buildAdditionalRegisters(RegistrationContext ctx, EntryCollector builder) {
        super.buildAdditionalRegisters(ctx, builder);
        builder.register(Registry.MENU_REGISTRY, this.menu);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            BlockMenu.openMenu(sp, this.menu, new TranslatableComponent("screen." + MenuBlock.this.mod.modid + "." + Objects.requireNonNull(MenuBlock.this.getRegistryName()).getPath()), pos);
        }
        return InteractionResult.SUCCESS;
    }
}
