package io.github.noeppi_noeppi.libx.base.decoration;

import io.github.noeppi_noeppi.libx.base.BlockBase;
import io.github.noeppi_noeppi.libx.mod.ModX;
import io.github.noeppi_noeppi.libx.registration.Registerable;
import io.github.noeppi_noeppi.libx.registration.RegistrationContext;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A block that registers some decoration blocks with it based on a {@link DecorationContext}.
 */
public class DecoratedBlock extends BlockBase {

    private final DecorationContext context;
    private final Map<DecorationType<?>, Object> elements;
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
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
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
            //noinspection unchecked
            return (T) this.elements.get(type);
        } else {
            throw new NoSuchElementException("Decoration context " + this.context + " has no element of type " + type.name() + ": " + type);
        }
    }
}
