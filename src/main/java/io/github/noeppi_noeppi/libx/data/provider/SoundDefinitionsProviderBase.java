package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * A base class for sound definition provider. Adds methods to add multiple sounds with same name at once.
 */
public abstract class SoundDefinitionsProviderBase extends SoundDefinitionsProvider {

    protected final ModX mod;

    public SoundDefinitionsProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper helper) {
        super(generator, mod.modid, helper);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " sound definitions";
    }

    @Override
    public void registerSounds() {
        this.setup();
    }

    protected abstract void setup();

    protected Sounds sound(String name, int amount) {
        Sounds sounds = new Sounds();
        for (int i = 0; i < amount; i++) {
            sounds.add(sound(this.mod.resource(name + i)));
        }
        return sounds;
    }

    protected Sounds sound(ResourceLocation name, int amount) {
        return this.sound(name, SoundDefinition.SoundType.SOUND, amount);
    }

    protected Sounds sound(ResourceLocation name, SoundDefinition.SoundType type, int amount) {
        Sounds sounds = new Sounds();
        for (int i = 0; i < amount; i++) {
            sounds.add(sound(new ResourceLocation(name.getNamespace(), name.getPath() + i), type));
        }
        return sounds;
    }

    protected static class Sounds extends ArrayList<SoundDefinition.Sound> {

        public Sounds volume(double volume) {
            return this.volume((float) volume);
        }

        public Sounds volume(float volume) {
            this.iterator().forEachRemaining(sound -> sound.volume(volume));
            return this;
        }

        public Sounds pitch(double pitch) {
            return this.pitch((float) pitch);
        }

        public Sounds pitch(float pitch) {
            this.iterator().forEachRemaining(sound -> sound.pitch(pitch));
            return this;
        }

        public Sounds weight(int weight) {
            this.iterator().forEachRemaining(sound -> sound.weight(weight));
            return this;
        }

        public Sounds streamSounds() {
            return this.stream(true);
        }

        public Sounds stream(boolean stream) {
            this.iterator().forEachRemaining(sound -> sound.stream(stream));
            return this;
        }

        public Sounds attenuationDistance(int attenuationDistance) {
            this.iterator().forEachRemaining(sound -> sound.attenuationDistance(attenuationDistance));
            return this;
        }

        public Sounds preload() {
            return this.preload(true);
        }

        public Sounds preload(boolean preload) {
            this.iterator().forEachRemaining(sound -> sound.preload(preload));
            return this;
        }

        public SoundDefinition asDefinition() {
            SoundDefinition definition = SoundDefinition.definition();
            this.iterator().forEachRemaining(definition::with);
            return definition;
        }
    }
}
