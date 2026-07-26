package org.moddingx.libx.registration;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.moddingx.libx.mod.ModXRegistration;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * An extension of {@link RegistrationContext} that is used during setup phase.
 */
public final class SetupContext extends RegistrationContext {
    
    private final Consumer<Runnable> enqueue;

    public SetupContext(RegistrationContext ctx, Consumer<Runnable> enqueue) {
        this(ctx.mod(), ctx.id(), ctx.key().orElse(null), enqueue);
    }
    
    public SetupContext(ModXRegistration mod, Identifier id, @Nullable ResourceKey<?> key, Consumer<Runnable> enqueue) {
        super(mod, id, key);
        this.enqueue = enqueue;
    }

    /**
     * Enqueues a given {@link Runnable} into the synchronous work queue.
     */
    public void enqueue(Runnable action) {
        this.enqueue.accept(action);
    }
}
