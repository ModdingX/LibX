package org.moddingx.libx.impl.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.moddingx.libx.config.ConfigManager;

public class ConfigEvents {

    @OnlyIn(Dist.DEDICATED_SERVER)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void serverPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer && FMLLoader.getDist() == Dist.DEDICATED_SERVER) {
            ConfigManager.synchronize(serverPlayer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void clientPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && FMLLoader.getDist() == Dist.CLIENT) {
            for (ConfigImpl config : ConfigImpl.getAllConfigs()) {
                config.reloadClientWorldState();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void clientPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        for (ResourceLocation id : ConfigManager.configs()) {
            ConfigImpl config = ConfigImpl.getConfig(id);
            if (!config.clientConfig) {
                config.restore();
            }
        }
    }
}
