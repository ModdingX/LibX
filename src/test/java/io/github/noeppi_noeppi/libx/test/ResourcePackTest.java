package io.github.noeppi_noeppi.libx.test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.impl.datapack.LibXDatapack;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ResourcePackTest {
    
    @Test
    public void testPackVersion() throws Throwable {
        try (Reader in = new InputStreamReader(Objects.requireNonNull(LibX.class.getResourceAsStream("/pack.mcmeta"), "pack.mcmeta file not found"))) {
            JsonObject packInfo = new GsonBuilder().create().fromJson(in, JsonObject.class);
            int packVersion = packInfo.get("pack").getAsJsonObject().get("pack_format").getAsInt();
            assertEquals(packVersion, LibXDatapack.PACK_VERSION, "Dynamic datapack version does not match version set in pack.mcmeta");
        }
    }
}
