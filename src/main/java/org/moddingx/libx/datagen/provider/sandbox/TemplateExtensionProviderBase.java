package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.structure.PoolExtension;

/**
 * SandBox provider for {@link PoolExtension template pool extensions}.
 *
 * This provider must run in the {@link DatagenStage#EXTENSION_SETUP extension setup} stage.
 */
public abstract non-sealed class TemplateExtensionProviderBase extends AnyTemplateProviderBase {

    protected TemplateExtensionProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.EXTENSION_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " template extensions";
    }

    /**
     * Creates a builder for a {@link PoolExtension} on the given pool.
     */
    public ExtensionBuilder extension(Holder<StructureTemplatePool> pool) {
        return this.extension(pool.unwrapKey().orElseThrow(() -> new IllegalStateException("Can't make biome surface: unbound biome holder: " + pool)));
    }
    
    /**
     * Creates a builder for a {@link PoolExtension} on the given pool.
     */
    public ExtensionBuilder extension(ResourceKey<StructureTemplatePool> pool) {
        return new ExtensionBuilder(pool);
    }
    
    public final class ExtensionBuilder extends TemplateBuilder<ExtensionBuilder> {

        private final ResourceKey<StructureTemplatePool> parent;
        private boolean required;
        
        private ExtensionBuilder(ResourceKey<StructureTemplatePool> parent) {
            this.parent = parent;
        }

        @Override
        protected ExtensionBuilder self() {
            return this;
        }

        /**
         * Marks this {@link PoolExtension} as required, causing an exception when the target pool is not present
         * in the registry.
         */
        public ExtensionBuilder required() {
            this.required = true;
            return this;
        }

        /**
         * Builds the {@link PoolExtension}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<PoolExtension> build() {
            PoolExtension ext = new PoolExtension(this.parent, this.required, this.elements());
            return TemplateExtensionProviderBase.this.registries.writableRegistry(SandBox.TEMPLATE_POOL_EXTENSION).createIntrusiveHolder(ext);
        }
    }
}
