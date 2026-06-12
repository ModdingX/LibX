package org.moddingx.libx.impl.config;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.moddingx.libx.config.ConfigManager;

public class ConfigEventsClient {

    @SubscribeEvent
    public void clientPlayerLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        for (Identifier id : ConfigManager.configs()) {
            ConfigImpl config = ConfigImpl.getConfig(id);
            if (!config.clientConfig) {
                config.restore();
            }
        }
    }
}
