package io.github.noeppi_noeppi.libx.data.provider;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A base class for sound definition provider. Adds methods to add multiple sounds with same name at once.
 */
public abstract class SoundDefinitionsProviderBase extends SoundDefinitionsProvider {

    protected final ModX mod;

    private final Set<SoundEvent> blacklist = new HashSet<>();

    public SoundDefinitionsProviderBase(ModX mod, DataGenerator generator, ExistingFileHelper helper) {
        super(generator, mod.modid, helper);
        this.mod = mod;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " sound definitions";
    }

    /**
     * This sound event will not be processed by the default generator
     */
    protected void manualSound(SoundEvent soundEvent) {
        this.blacklist.add(soundEvent);
    }

    @Override
    public void registerSounds() {
        this.setup();

        for (ResourceLocation id : ForgeRegistries.SOUND_EVENTS.getKeys()) {
            SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(id);
            if (soundEvent != null && this.mod.modid.equals(id.getNamespace()) && !this.blacklist.contains(soundEvent)) {
                this.defaultSoundDefinition(id, soundEvent);
            }
        }
    }

    protected abstract void setup();

    protected void defaultSoundDefinition(ResourceLocation id, SoundEvent soundEvent) {
        String subtitle = "subtitle." + id.getNamespace() + "." + id.getPath().replace("/", ".");
        this.add(id, definition()
                .with(sound(id))
                .subtitle(subtitle));
    }

    /**
     * The name for the sound with the amount of different sound files.
     */
    protected Sounds sound(String name, int amount) {
        Sounds sounds = new Sounds();
        for (int i = 0; i < amount; i++) {
            sounds.add(sound(this.mod.resource(name + i)));
        }
        return sounds;
    }

    /**
     * The resource location for the sound with the amount of different sound files.
     */
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

    /**
     * A helper class to allow setting e.g. the volume or pitch for all the sounds in the list.
     */
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

        /**
         * Same as {@link net.minecraftforge.common.data.SoundDefinition.Sound#stream()} but for all sound files.
         */
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

        /**
         * Puts all sounds in one sound definition.
         */
        public SoundDefinition asDefinition() {
            SoundDefinition definition = SoundDefinition.definition();
            this.iterator().forEachRemaining(definition::with);
            return definition;
        }
    }
}
