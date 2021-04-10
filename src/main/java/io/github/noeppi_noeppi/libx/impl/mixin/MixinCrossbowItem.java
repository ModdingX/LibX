package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.event.PlayerFindAmmoEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrossbowItem.class)
public class MixinCrossbowItem {
    
    @Redirect(
            method = "Lnet/minecraft/item/CrossbowItem;onItemRightClick(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
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
    
    @Redirect(
            method = "Lnet/minecraft/item/CrossbowItem;hasAmmo(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;findAmmo(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack findAmmo2(LivingEntity living, ItemStack shootable) {
        if (living instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) living; 
            ItemStack ammo = player.findAmmo(shootable);
            PlayerFindAmmoEvent event = new PlayerFindAmmoEvent(player, shootable, ammo);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return player.isCreative() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
            } else {
                return event.getAmmo();
            }
        } else {
            return living.findAmmo(shootable);
        }
    }
}
