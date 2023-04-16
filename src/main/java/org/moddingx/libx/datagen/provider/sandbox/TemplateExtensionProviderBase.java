package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.sandbox.SandBox;
import org.moddingx.libx.sandbox.structure.PoolExtension;

public abstract non-sealed class TemplateExtensionProviderBase extends AnyTemplateProviderBase {

    protected TemplateExtensionProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.EXTENSION_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " template extensions";
    }
    
    public ExtensionBuilder extension(Holder<StructureTemplatePool> pool) {
        return this.extension(pool.unwrapKey().orElseThrow(() -> new IllegalStateException("Can't make biome surface: unbound biome holder: " + pool)));
    }
    
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
        
        public ExtensionBuilder required() {
            this.required = true;
            return this;
        }
        
        public Holder<PoolExtension> build() {
            PoolExtension ext = new PoolExtension(this.parent, this.required, this.elements());
            return TemplateExtensionProviderBase.this.registries.writableRegistry(SandBox.TEMPLATE_POOL_EXTENSION).createIntrusiveHolder(ext);
        }
    }
}
