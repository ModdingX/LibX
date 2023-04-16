package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;

public abstract non-sealed class TemplateProviderBase extends AnyTemplateProviderBase {

    protected TemplateProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " templates";
    }
    
    public PoolBuilder template() {
        return this.template(this.holder(Pools.EMPTY));
    }
    
    public PoolBuilder template(Holder<StructureTemplatePool> fallback) {
        return new PoolBuilder(fallback);
    }

    public final class PoolBuilder extends TemplateBuilder<PoolBuilder> {

        private final Holder<StructureTemplatePool> fallback;
        
        private PoolBuilder(Holder<StructureTemplatePool> fallback) {
            this.fallback = fallback;
        }

        @Override
        protected PoolBuilder self() {
            return this;
        }
        
        public Holder<StructureTemplatePool> build() {
            StructureTemplatePool pool = new StructureTemplatePool(this.fallback, this.elements());
            return TemplateProviderBase.this.registries.writableRegistry(Registries.TEMPLATE_POOL).createIntrusiveHolder(pool);
        }
    }
}
