package io.github.noeppi_noeppi.libx.impl.config;

import io.github.noeppi_noeppi.libx.config.ConfigManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ConfigEvents {

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void serverPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide && event.getPlayer() instanceof ServerPlayer serverPlayer && FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            ConfigManager.forceResync(serverPlayer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void clientPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide && FMLEnvironment.dist == Dist.CLIENT) {
            for (ConfigImpl config : ConfigImpl.getAllConfigs()) {
                config.reloadClientWorldState();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void clientPlayerLeave(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        for (ResourceLocation id : ConfigManager.configs()) {
            ConfigImpl config = ConfigImpl.getConfig(id);
            if (!config.clientConfig) {
                config.restore();
            }
        }
    }
}
