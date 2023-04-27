package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

/**
 * SandBox provider for {@link StructureTemplatePool template pools}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract non-sealed class TemplateProviderBase extends AnyTemplateProviderBase {

    protected TemplateProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " templates";
    }

    /**
     * Creates a new {@link PoolBuilder} with an empty fallback.
     */
    public PoolBuilder template() {
        return this.template(this.holder(Pools.EMPTY));
    }
    
    /**
     * Creates a new {@link PoolBuilder} with with the given fallback.
     */
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

        /**
         * Builds the {@link StructureTemplatePool}.
         * 
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<StructureTemplatePool> build() {
            StructureTemplatePool pool = new StructureTemplatePool(this.fallback, this.elements());
            return TemplateProviderBase.this.registries.writableRegistry(Registries.TEMPLATE_POOL).createIntrusiveHolder(pool);
        }
    }
}
