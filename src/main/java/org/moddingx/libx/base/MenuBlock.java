package org.moddingx.libx.base;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.menu.BlockMenu;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

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
    public void registerAdditional(RegistrationContext ctx, Registerable.EntryCollector builder) {
        super.registerAdditional(ctx, builder);
        builder.register(Registry.MENU_REGISTRY, this.menu);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void initTracking(RegistrationContext ctx, TrackingCollector builder) throws ReflectiveOperationException {
        super.initTracking(ctx, builder);
        builder.track(ForgeRegistries.MENU_TYPES, MenuBlock.class.getDeclaredField("menu"));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            BlockMenu.openMenu(sp, this.menu, Component.translatable("screen." + MenuBlock.this.mod.modid + "." + Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(MenuBlock.this)).getPath()), pos);
        }
        return InteractionResult.SUCCESS;
    }
}
