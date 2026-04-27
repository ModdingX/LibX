package org.moddingx.libx.impl.config;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.moddingx.libx.config.ConfigManager;

public class ConfigEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void serverPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer serverPlayer && FMLEnvironment.getDist() == Dist.DEDICATED_SERVER) {
            ConfigManager.synchronize(serverPlayer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void clientPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide() && FMLEnvironment.getDist() == Dist.CLIENT) {
            for (ConfigImpl config : ConfigImpl.getAllConfigs()) {
                config.reloadClientWorldState();
            }
        }
    }
}
