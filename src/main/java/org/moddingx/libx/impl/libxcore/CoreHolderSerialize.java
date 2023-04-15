package org.moddingx.libx.impl.libxcore;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import org.moddingx.libx.impl.datagen.registries.DatagenRegistry;

public class CoreHolderSerialize {

    /**
     * Patched into {@link Holder.Reference#canSerializeIn(HolderOwner)} at head.
     * Passing all the arguments from the source method. Returning true will skip default behaviour.
     */
    public static boolean forceSerializeIn(Holder.Reference<?> ref, HolderOwner<?> owner) {
        return owner instanceof DatagenRegistry<?>;
    }
}
