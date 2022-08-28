package org.moddingx.libx.base.decoration;

import net.minecraft.world.item.Item;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A block that registers some decoration blocks with it based on a {@link DecorationContext}.
 */
public class DecoratedBlock extends BlockBase {

    private final DecorationContext context;
    private final Map<DecorationType<?>, DecorationType.DecorationElement<?, ?>> elements;
    private final Map<String, Registerable> registerMap;
    
    public DecoratedBlock(ModX mod, DecorationContext context, Properties properties) {
        this(mod, context, properties, new Item.Properties());
    }

    public DecoratedBlock(ModX mod, DecorationContext context, Properties properties, Item.Properties itemProperties) {
        super(mod, properties, itemProperties);
        this.context = context;
        DecorationContext.RegistrationInfo info = context.register(this.mod, this);
        this.elements = info.elementMap();
        this.registerMap = info.registerMap();
    }
    
    @Override
    @OverridingMethodsMustInvokeSuper
    public void registerAdditional(RegistrationContext ctx, Registerable.EntryCollector builder) {
        super.registerAdditional(ctx, builder);
        for (Map.Entry<String, Registerable> entry : this.registerMap.entrySet()) {
            builder.registerNamed(null, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the {@link DecorationContext} used for this block.
     */
    public DecorationContext getContext() {
        return this.context;
    }

    /**
     * Gets whether this block has a given type of decoration.
     */
    public boolean has(DecorationType<?> type) {
        return this.context.has(type) && this.elements.containsKey(type);
    }

    /**
     * Gets an element registered together with this block.
     */
    @Nonnull
    public <T> T get(DecorationType<T> type) {
        if (type == DecorationType.BASE) {
            //noinspection unchecked
            return (T) this;
        } else if (this.has(type)) {
            return (T) this.elements.get(type).element();
        } else {
            throw new NoSuchElementException("Decoration context " + this.context + " has no element of type " + type.name() + ": " + type);
        }
    }
}
