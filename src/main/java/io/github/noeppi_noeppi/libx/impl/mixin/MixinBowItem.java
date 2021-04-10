package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.event.PlayerFindAmmoEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BowItem.class)
public class MixinBowItem {

    @Redirect(
            method = {
                    "Lnet/minecraft/item/BowItem;onPlayerStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
                    "Lnet/minecraft/item/BowItem;onItemRightClick(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;findAmmo(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"
            )
    )
    public ItemStack findAmmo(PlayerEntity player, ItemStack shootable) {
        ItemStack ammo = player.findAmmo(shootable);
        PlayerFindAmmoEvent event = new PlayerFindAmmoEvent(player, shootable, ammo);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return player.isCreative() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
        } else {
            return event.getAmmo();
        }
    }
}
