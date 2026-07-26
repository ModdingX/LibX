package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.EasingType;
import net.minecraft.util.KeyframeTrack;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import java.util.function.Consumer;

/**
 * SandBox provider for {@link Timeline timelines}.
 *
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class TimelineProviderBase extends RegistryProviderBase {

    /**
     * The length of a vanilla overworld day in ticks. Commonly used as a
     * {@link TimelineBuilder#period(int) period} for timelines on the {@link WorldClocks#OVERWORLD overworld clock}.
     */
    public static final int DAY_TICKS = 24000;

    protected TimelineProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Override
    public final String getName() {
        return this.mod.modid + " timelines";
    }

    /**
     * Creates a new builder for a timeline that is driven by the {@link WorldClocks#OVERWORLD overworld clock}.
     */
    public TimelineBuilder timeline() {
        return this.timeline(WorldClocks.OVERWORLD);
    }

    /**
     * Creates a new builder for a timeline that is driven by the given {@link WorldClock clock}. The current
     * registry set must contain a matching element.
     */
    public TimelineBuilder timeline(ResourceKey<WorldClock> clock) {
        return this.timeline(this.holder(clock));
    }

    /**
     * Creates a new builder for a timeline that is driven by the given {@link WorldClock clock}.
     */
    public TimelineBuilder timeline(Holder<WorldClock> clock) {
        return new TimelineBuilder(clock);
    }

    public class TimelineBuilder {

        private final Timeline.Builder builder;

        private TimelineBuilder(Holder<WorldClock> clock) {
            this.builder = Timeline.builder(clock);
        }

        /**
         * Sets the period of this timeline in ticks. The timeline repeats after that many ticks and all its
         * keyframes and time markers must lie inside {@code [0; periodTicks]}.
         *
         * If this is not set, the timeline does not repeat and runs once over the entire lifetime of the world.
         * Use {@link TimelineProviderBase#DAY_TICKS} for a timeline that repeats every overworld day.
         */
        public TimelineBuilder period(int periodTicks) {
            this.builder.setPeriodTicks(periodTicks);
            return this;
        }

        /**
         * Adds a track that overrides the given {@link EnvironmentAttribute attribute} with the keyframed values.
         *
         * The keyframes are added through the given consumer. They must be sorted by their tick and at least one
         * keyframe is required. The default easing is {@link EasingType#LINEAR}, use
         * {@link KeyframeTrack.Builder#setEasing(EasingType)} to change it.
         */
        public <T> TimelineBuilder track(EnvironmentAttribute<T> attribute, Consumer<KeyframeTrack.Builder<T>> track) {
            this.builder.addTrack(attribute, track);
            return this;
        }

        /**
         * Adds a track that combines the current value of the given {@link EnvironmentAttribute attribute} with the
         * keyframed values through the given {@link AttributeModifier modifier}, for example
         * {@code ColorModifier.MULTIPLY_RGB} to tint a color attribute or {@code FloatModifier.MULTIPLY} to scale a
         * float attribute. The modifier must be allowed for the attributes type.
         *
         * The keyframes are added through the given consumer. They must be sorted by their tick and at least one
         * keyframe is required. Note that the keyframe values are the arguments of the modifier, which need not have
         * the same type as the attribute itself.
         */
        public <T, A> TimelineBuilder track(EnvironmentAttribute<T> attribute, AttributeModifier<T, A> modifier, Consumer<KeyframeTrack.Builder<A>> track) {
            this.builder.addModifierTrack(attribute, modifier, track);
            return this;
        }

        /**
         * Adds a track that overrides the given {@link EnvironmentAttribute attribute} with a constant value for the
         * entire timeline.
         */
        public <T> TimelineBuilder constant(EnvironmentAttribute<T> attribute, T value) {
            return this.track(attribute, track -> track.setEasing(EasingType.CONSTANT).addKeyframe(0, value));
        }

        /**
         * Adds a {@link ClockTimeMarker time marker} at the given tick to the clock of this timeline. The marker is
         * not shown in the {@code /time set} command.
         *
         * A time marker key may only be defined once per clock, so a marker like {@link ClockTimeMarkers#DAY} can not
         * be redefined for the {@link WorldClocks#OVERWORLD overworld clock}.
         */
        public TimelineBuilder timeMarker(ResourceKey<ClockTimeMarker> marker, int ticks) {
            return this.timeMarker(marker, ticks, false);
        }

        /**
         * Adds a {@link ClockTimeMarker time marker} at the given tick to the clock of this timeline.
         *
         * A time marker key may only be defined once per clock, so a marker like {@link ClockTimeMarkers#DAY} can not
         * be redefined for the {@link WorldClocks#OVERWORLD overworld clock}.
         *
         * @param showInCommands whether the marker can be used as an argument to the {@code /time set} command.
         */
        public TimelineBuilder timeMarker(ResourceKey<ClockTimeMarker> marker, int ticks, boolean showInCommands) {
            this.builder.addTimeMarker(marker, ticks, showInCommands);
            return this;
        }

        /**
         * Builds the {@link Timeline}.
         *
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added to the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<Timeline> build() {
            Timeline timeline = this.builder.build();
            return TimelineProviderBase.this.registries.writableRegistry(Registries.TIMELINE).createIntrusiveHolder(timeline);
        }
    }
}
