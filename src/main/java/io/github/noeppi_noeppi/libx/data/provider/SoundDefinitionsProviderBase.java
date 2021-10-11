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
import java.util.List;
import java.util.Set;

/**
 * A base class for sound definition provider. Adds methods to add multiple sounds with same name at once.
 */
public abstract class SoundDefinitionsProviderBase extends SoundDefinitionsProvider {

    protected final ModX mod;

    private final Set<SoundEvent> blacklist = new HashSet<>();
    private final Set<Sounds> sounds = new HashSet<>();

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
    protected void ignore(SoundEvent soundEvent) {
        this.blacklist.add(soundEvent);
    }

    @Override
    public void registerSounds() {
        this.setup();
        this.sounds.forEach(Sounds::buildDefinition);

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
    protected SoundDefinition sound(String name, int amount) {
        return this.sound(this.mod.resource(name), amount);
    }

    /**
     * The resource location for the sound with the amount of different sound files.
     */
    protected SoundDefinition sound(ResourceLocation location, int amount) {
        return this.sound(location, SoundDefinition.SoundType.SOUND, amount);
    }

    protected SoundDefinition sound(ResourceLocation location, SoundDefinition.SoundType type, int amount) {
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(location);

        if (soundEvent != null) {
            this.ignore(soundEvent);
        }

        Sounds sounds = new Sounds();
        for (int i = 0; i < amount; i++) {
            sounds.add(sound(this.transformLocation(location, i), type));
        }

        this.sounds.add(sounds);
        return sounds.definition;
    }

    /**
     * Used to transform the given location into a new location.
     * Override this in case you have another naming pattern for your sound files.
     */
    protected ResourceLocation transformLocation(ResourceLocation location, int index) {
        return new ResourceLocation(location.getNamespace(), location.getPath() + index);
    }

    /**
     * A helper class to allow setting e.g. the volume or pitch for all the sounds in the list.
     */
    protected static class Sounds {

        private final List<SoundDefinition.Sound> sounds = new ArrayList<>();
        private final SoundDefinition definition = SoundDefinition.definition();

        /**
         * See {@link SoundDefinition.Sound#volume(double)} but for all sound files.
         */
        public void volume(double volume) {
            this.volume((float) volume);
        }

        /**
         * See {@link SoundDefinition.Sound#volume(float)} but for all sound files.
         */
        public void volume(float volume) {
            this.sounds.forEach(sound -> sound.volume(volume));
        }

        /**
         * See {@link SoundDefinition.Sound#pitch(double)} but for all sound files.
         */
        public void pitch(double pitch) {
            this.pitch((float) pitch);
        }

        /**
         * See {@link SoundDefinition.Sound#pitch(float)} but for all sound files.
         */
        public void pitch(float pitch) {
            this.sounds.forEach(sound -> sound.pitch(pitch));
        }

        /**
         * See {@link SoundDefinition.Sound#weight(int)} but for all sound files.
         */
        public void weight(int weight) {
            this.sounds.forEach(sound -> sound.weight(weight));
        }

        /**
         * See {@link SoundDefinition.Sound#stream()} but for all sound files.
         */
        public void stream() {
            this.stream(true);
        }

        /**
         * See {@link SoundDefinition.Sound#stream(boolean)} but for all sound files.
         */
        public void stream(boolean stream) {
            this.sounds.forEach(sound -> sound.stream(stream));
        }

        /**
         * See {@link SoundDefinition.Sound#attenuationDistance(int)} but for all sound files.
         */
        public void attenuationDistance(int attenuationDistance) {
            this.sounds.forEach(sound -> sound.attenuationDistance(attenuationDistance));
        }

        /**
         * See {@link SoundDefinition.Sound#preload()} but for all sound files.
         */
        public void preload() {
            this.preload(true);
        }

        /**
         * See {@link SoundDefinition.Sound#preload(boolean)} but for all sound files.
         */
        public void preload(boolean preload) {
            this.sounds.forEach(sound -> sound.preload(preload));
        }

        private void add(SoundDefinition.Sound sound) {
            this.sounds.add(sound);
        }

        private void buildDefinition() {
            this.sounds.forEach(this.definition::with);
        }
    }
}
