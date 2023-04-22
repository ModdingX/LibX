package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.moddingx.libx.datagen.DatagenContext;

import javax.annotation.Nullable;

public abstract class ChestLootProviderBase extends LootProviderBase<String> {
    
    protected ChestLootProviderBase(DatagenContext ctx) {
        super(ctx, "chests", LootContextParamSets.CHEST, ctx.mod()::resource);
    }

    @Nullable
    @Override
    protected LootTable.Builder defaultBehavior(String item) {
        return null;
    }
}
