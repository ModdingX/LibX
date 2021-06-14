package io.github.noeppi_noeppi.libx.impl.mixin;

import io.github.noeppi_noeppi.libx.event.DatapacksReloadedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Inject(
            method = "Lnet/minecraft/server/management/PlayerList;reloadResources()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/management/PlayerList;sendPacketToAllPlayers(Lnet/minecraft/network/IPacket;)V"
            )
    )
    public void reloadResources(CallbackInfo ci) {
        //noinspection ConstantConditions
        MinecraftServer server = ((PlayerList) (Object) (this)).getServer();
        //noinspection ConstantConditions
        if (server != null && server.getDataPackRegistries() != null) {
            MinecraftForge.EVENT_BUS.post(new DatapacksReloadedEvent(server, server.getDataPackRegistries()));
        }
    }
}
