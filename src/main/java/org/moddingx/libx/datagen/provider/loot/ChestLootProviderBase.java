package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nullable;

public abstract class ChestLootProviderBase extends LootProviderBase<String> {
    
    protected ChestLootProviderBase(ModX mod, PackOutput packOutput) {
        super(mod, packOutput, "chests", LootContextParamSets.CHEST, mod::resource);
    }

    @Nullable
    @Override
    protected LootTable.Builder defaultBehavior(String item) {
        return null;
    }
}
