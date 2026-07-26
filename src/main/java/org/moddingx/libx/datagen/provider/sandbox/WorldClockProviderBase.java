package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

/**
 * SandBox provider for {@link WorldClock world clocks}.
 *
 * As there is nothing to configure, this provider has no builder: {@link #clock()} directly creates a new clock.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class WorldClockProviderBase extends RegistryProviderBase {

    protected WorldClockProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " world clocks";
    }

    /**
     * Creates a new {@link WorldClock}.
     *
     * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
     * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
     * {@code public}, non-{@code static} field inside the provider.
     */
    public Holder<WorldClock> clock() {
        return this.registries.writableRegistry(Registries.WORLD_CLOCK).createIntrusiveHolder(new WorldClock());
    }
}
