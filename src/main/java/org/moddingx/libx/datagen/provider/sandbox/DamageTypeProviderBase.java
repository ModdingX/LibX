package org.moddingx.libx.datagen.provider.sandbox;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.DatagenStage;
import org.moddingx.libx.datagen.provider.RegistryProviderBase;

import javax.annotation.Nonnull;

/**
 * SandBox provider for {@link DamageType damage type}.
 * <p>
 * This provider must run in the {@link DatagenStage#REGISTRY_SETUP registry setup} stage.
 */
public abstract class DamageTypeProviderBase extends RegistryProviderBase {

    public DamageTypeProviderBase(DatagenContext ctx) {
        super(ctx, DatagenStage.REGISTRY_SETUP);
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " damage types";
    }

    public DamageTypeBuilder damageType(String msgId, float exhaustion) {
        return new DamageTypeBuilder(msgId, exhaustion);
    }

    public class DamageTypeBuilder {

        private final String msgId;
        private final float exhaustion;
        private DamageScaling scaling;
        private DamageEffects effects;
        private DeathMessageType deathMessageType;

        public DamageTypeBuilder(String msgId, float exhaustion) {
            this.msgId = msgId;
            this.exhaustion = exhaustion;
        }

        public DamageTypeBuilder scaling(DamageScaling scaling) {
            this.scaling = scaling;
            return this;
        }

        public DamageTypeBuilder effects(DamageEffects effects) {
            this.effects = effects;
            return this;
        }

        public DamageTypeBuilder deathMessageType(DeathMessageType deathMessageType) {
            this.deathMessageType = deathMessageType;
            return this;
        }

        /**
         * Builds the {@link DamageType}.
         * <p>
         * This method returns an {@link Holder.Reference.Type#INTRUSIVE intrusive holder} that must be properly
         * added the registry. {@link RegistryProviderBase} does this automatically if the result is stored in a
         * {@code public}, non-{@code static} field inside the provider.
         */
        public Holder<DamageType> build() {
            DamageType damageType = new DamageType(this.msgId, this.scaling, this.exhaustion, this.effects, this.deathMessageType);
            return DamageTypeProviderBase.this.registries.writableRegistry(Registries.DAMAGE_TYPE).createIntrusiveHolder(damageType);
        }
    }
}
