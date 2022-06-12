package org.moddingx.libx.test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Test;
import org.moddingx.libx.LibX;
import org.moddingx.libx.impl.datapack.LibXDatapack;

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
            int packVersion = packInfo.get("pack").getAsJsonObject().get("pack_format").getAsInt();
            assertEquals(SharedConstants.DATA_PACK_FORMAT, packVersion, "pack.mcmeta does not match current data version");
            assertEquals(SharedConstants.DATA_PACK_FORMAT, LibXDatapack.PACK_VERSION, "Dynamic datapack version does not match current data version");
        }
    }
}
