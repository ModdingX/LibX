package org.moddingx.libx.impl.datagen.load;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DatagenLootLoader {
    
    private static Map<ResourceLocation, LootTable> lootTables;
    
    public static Map<ResourceLocation, LootTable> loadLootTables(@Nullable ExistingFileHelper fileHelper) {
        if (lootTables == null) {
            if (fileHelper == null) throw new RuntimeException("Can't load loot tables without file helper.");
            ResourceManager mgr = DatagenLoader.resources(fileHelper, PackType.SERVER_DATA);
            Map<ResourceLocation, JsonElement> jsonData = new HashMap<>();
            SimpleJsonResourceReloadListener.scanDirectory(mgr, LootDataType.TABLE.directory(), LootDataType.TABLE.parser(), jsonData);
            
            Map<ResourceLocation, LootTable> lootData = new HashMap<>();
            jsonData.forEach((id, json) -> LootDataType.TABLE.deserialize(id, json, mgr).ifPresent(lootTable -> lootData.put(id, lootTable)));
            lootTables = Collections.unmodifiableMap(lootData);
        }
        return lootTables;
    }
}
