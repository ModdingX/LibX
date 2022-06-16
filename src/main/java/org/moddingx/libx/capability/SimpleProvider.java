package org.moddingx.libx.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A simple {@link ICapabilityProvider capability provider} for a single capability that is
 * lazily initialised. For all other capabilities, a parent provider will be queried.
 */
public class SimpleProvider<A> implements ICapabilityProvider {

    @Nullable
    private final ICapabilityProvider parent;
    private final Capability<A> capability;
    private final LazyValue<A> value;

    /**
     * Creates a new {@link SimpleProvider} that provides the value from the given supplier for
     * the given capability. The supplier will only be invoked once.
     */
    public SimpleProvider(Capability<A> capability, Supplier<A> value) {
        this(null, capability, value);
    }
    
    /**
     * Creates a new {@link SimpleProvider} that provides the given value for the given capability.
     */
    public SimpleProvider(Capability<A> capability, LazyValue<A> value) {
        this(null, capability, value);
    }

    /**
     * Creates a new {@link SimpleProvider} that provides the value from the given supplier for
     * the given capability. The supplier will only be invoked once.
     * 
     * @param parent The parent provider used for other capabilities.
     */
    public SimpleProvider(@Nullable ICapabilityProvider parent, Capability<A> capability, Supplier<A> value) {
        this(parent, capability, new LazyValue<>(value));
    }
    
    /**
     * Creates a new {@link SimpleProvider} that provides the given value for the given capability.
     * 
     * @param parent The parent provider used for other capabilities.
     */
    public SimpleProvider(@Nullable ICapabilityProvider parent, Capability<A> capability, LazyValue<A> value) {
        this.parent = parent;
        this.capability = capability;
        this.value = value;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (this.capability == cap) {
            return LazyOptional.of(() -> Objects.requireNonNull(this.value.get())).cast();
        } else {
            return this.parent != null ? this.parent.getCapability(cap, side) : LazyOptional.empty();
        }
    }
}
