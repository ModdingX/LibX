package org.moddingx.libx.base.decoration;

import net.minecraft.world.item.Item;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.SetupContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A block that registers some decoration blocks with it based on a {@link DecorationContext}.
 */
public class DecoratedBlock extends BlockBase {

    private final DecorationContext context;
    
    @Nullable private DecorationMaterial.MaterialProperties materialProperties;
    @Nullable private DecorationContext.RegistrationInfo info;
    
    public DecoratedBlock(ModX mod, DecorationContext context, Properties materialProperties) {
        this(mod, context, materialProperties, new Item.Properties());
    }

    public DecoratedBlock(ModX mod, DecorationContext context, Properties materialProperties, Item.Properties itemProperties) {
        super(mod, materialProperties, itemProperties);
        this.context = context;
        this.info = null;
    }
    
    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, Registerable.EntryCollector builder) {
        this.init(ctx);
        super.registerAdditional(ctx, builder);
        for (Map.Entry<String, Registerable> entry : Objects.requireNonNull(this.info).registerMap().entrySet()) {
            builder.registerNamed(null, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void registerCommon(SetupContext ctx) {
        this.init(ctx);
        //noinspection DataFlowIssue
        ctx.enqueue(this.materialProperties::register);
    }

    private void init(RegistrationContext ctx) {
        if (this.materialProperties == null) {
            this.materialProperties = this.context.material().init(ctx.id());
        }
        if (this.info == null) {
            this.info = this.context.register(this.mod, this);
        }
    }

    /**
     * Gets the {@link DecorationContext} used for this block.
     */
    public DecorationContext getContext() {
        return this.context;
    }

    public DecorationMaterial.MaterialProperties getMaterialProperties() {
        if (this.materialProperties == null) throw new IllegalStateException("Decorated block not initialised.");
        return this.materialProperties;
    }

    /**
     * Gets whether this block has a given type of decoration.
     */
    public boolean has(DecorationType<?> type) {
        if (this.info == null) throw new IllegalStateException("Decorated block not initialised.");
        return this.context.has(type) && this.info.elementMap().containsKey(type);
    }

    /**
     * Gets an element registered together with this block.
     */
    @Nonnull
    public <T> T get(DecorationType<T> type) {
        if (this.info == null) throw new IllegalStateException("Decorated block not initialised.");
        if (type == DecorationType.BASE) {
            //noinspection unchecked
            return (T) this;
        } else if (this.has(type)) {
            return (T) this.info.elementMap().get(type).element();
        } else {
            throw new NoSuchElementException("Decoration context " + this.context + " has no element of type " + type.name() + ": " + type);
        }
    }
}
