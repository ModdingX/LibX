package org.moddingx.libx.datagen.provider.loot;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;

public abstract class GlobalLootProvider extends GlobalLootModifierProvider {
    
    protected final ModX mod;
    
    public GlobalLootProvider(ModX mod, DataGenerator gen) {
        super(gen, mod.modid);
        this.mod = mod;
    }

    protected abstract void setup();
    
    @Override
    protected final void start() {
        this.setup();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " loot modifiers";
    }
}
