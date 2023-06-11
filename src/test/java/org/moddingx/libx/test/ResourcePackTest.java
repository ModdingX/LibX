package org.moddingx.libx.test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Test;
import org.moddingx.libx.LibX;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Test the datapack version in pack.mcmeta and the version for dynamic datapacks
public class ResourcePackTest {
    
    @Test
    @SuppressWarnings("deprecation")
    public void testPackVersion() throws Throwable {
        try (Reader in = new InputStreamReader(Objects.requireNonNull(LibX.class.getResourceAsStream("/pack.mcmeta"), "pack.mcmeta file not found"))) {
            JsonObject packInfo = new GsonBuilder().create().fromJson(in, JsonObject.class);
            int mainPackVersion = packInfo.get("pack").getAsJsonObject().get("pack_format").getAsInt();
            int resourcePackVersion = packInfo.get("pack").getAsJsonObject().get("forge:client_resources_pack_format").getAsInt();
            int dataPackVersion = packInfo.get("pack").getAsJsonObject().get("forge:server_data_pack_format").getAsInt();
            assertEquals(Math.max(SharedConstants.RESOURCE_PACK_FORMAT, SharedConstants.DATA_PACK_FORMAT), mainPackVersion, "pack.mcmeta does not match current resource/data version");
            assertEquals(SharedConstants.RESOURCE_PACK_FORMAT, resourcePackVersion, "pack.mcmeta does not match current resource version");
            assertEquals(SharedConstants.DATA_PACK_FORMAT, dataPackVersion, "pack.mcmeta does not match current data version");
        }
    }
}
