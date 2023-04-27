package org.moddingx.libx.datagen.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.damagesource.DamageType;
import org.moddingx.libx.LibX;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A base class for {@link DamageType damage type} providers
 */
public abstract class DamageTypeProviderBase implements DataProvider {

    protected final ModX mod;
    protected final PackTarget packTarget;
    private final Map<ResourceLocation, DamageType> damageTypes = new HashMap<>();

    public DamageTypeProviderBase(DatagenContext ctx) {
        this.mod = ctx.mod();
        this.packTarget = ctx.target();
    }

    public abstract void setup();

    @Nonnull
    @Override
    public String getName() {
        return this.mod.modid + " damage types";
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        this.setup();
        return CompletableFuture.allOf(this.damageTypes.entrySet().stream().map(entry -> {
            ResourceLocation id = entry.getKey();
            DamageType damageType = entry.getValue();
            Path path = this.packTarget.path(PackType.SERVER_DATA)
                    .resolve(id.getNamespace() + "/damage_types/" + id.getPath() + ".json");
            JsonElement jsonElement = DamageType.CODEC.encodeStart(JsonOps.INSTANCE, damageType)
                    .getOrThrow(false, LibX.logger::warn);
            return DataProvider.saveStable(cache, jsonElement, path);
        }).toArray(CompletableFuture[]::new));
    }

    /**
     * Adds a {@link DamageType damageType} to the provider. The location will be created dynamically.
     */
    public void damageType(DamageType damageType) {
        StringBuilder sb = new StringBuilder();
        for (char chr : damageType.msgId().toCharArray()) {
            if (Character.isUpperCase(chr)) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(chr));
        }

        this.damageType(this.mod.resource(sb.toString()), damageType);
    }

    /**
     * Adds a {@link DamageType damageType} to the provider with a given id to be used.
     *
     * @param id The location where to store the data
     */
    public void damageType(ResourceLocation id, DamageType damageType) {
        this.damageTypes.put(id, damageType);
    }
}
