package io.github.noeppi_noeppi.libx.codec;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.noeppi_noeppi.libx.impl.codec.EnumCodec;
import io.github.noeppi_noeppi.libx.impl.codec.ForgeRegistryCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Provides additional {@link Codec codecs}.
 */
public class MoreCodecs {

    /**
     * Gets a codec that encodes an {@link Enum enum} as a string.
     */
    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> clazz) {
        return EnumCodec.get(clazz);
    }
    
    /**
     * Gets a codec that encodes a {@link IForgeRegistryEntry} as a string using its registry name.
     */
    public static <T extends IForgeRegistryEntry<T>> Codec<T> registry(IForgeRegistry<T> registry) {
        return ForgeRegistryCodec.get(registry);
    }

    /**
     * A codec for the {@link Unit} constant that encodes to nothing.
     */
    public static final Codec<Unit> UNIT = new Codec<>() {

        @Override
        public <T> DataResult<T> encode(Unit input, DynamicOps<T> ops, T prefix) {
            return DataResult.success(prefix);
        }

        @Override
        public <T> DataResult<Pair<Unit, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.success(Pair.of(Unit.INSTANCE, input));
        }
    };
    
    /**
     * A codec for a {@link ResourceLocation}.
     */
    public static final Codec<ResourceLocation> RESOURCE_LOCATION = new Codec<>() {

        @Override
        public <T> DataResult<T> encode(ResourceLocation input, DynamicOps<T> ops, T prefix) {
            return ops.mergeToPrimitive(prefix, ops.createString(input.toString()));
        }

        @Override
        public <T> DataResult<Pair<ResourceLocation, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input)
                    .flatMap(str -> CodecHelper.nonNull(ResourceLocation.tryParse(str), "Invalid resource location: " + str))
                    .map(r -> Pair.of(r, ops.empty()));
        }
    };

    /**
     * A codec that encodes a {@link CompoundTag} into a string. The {@link CompoundTag} will always be
     * encoded as a string, even if it is serialised to nbt.
     */
    public static final Codec<CompoundTag> COMPOUND_TAG = new PrimitiveCodec<>() {
        
        @Override
        public <T> DataResult<CompoundTag> read(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).flatMap(str -> {
                try {
                    return DataResult.success(TagParser.parseTag(str));
                } catch (CommandSyntaxException e) {
                    return DataResult.error("Not a compound tag: " + str);
                }
            });
        }
        
        @Override
        public <T> T write(DynamicOps<T> ops, CompoundTag value) {
            return ops.createString(value.toString());
        }
    };

    /**
     * A codec that encodes an {@link ItemStack} in a format similar to what is used in recipes.
     */
    public static final Codec<ItemStack> ITEM_STACK = RecordCodecBuilder.create(instance -> instance.group(
            registry(ForgeRegistries.ITEMS).fieldOf("item").forGetter(ItemStack::getItem),
            Codec.INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
            COMPOUND_TAG.fieldOf("tag").orElseGet(CompoundTag::new).forGetter(ItemStack::getTag)
    ).apply(instance, instance.stable((item, count, tag) -> {
        ItemStack stack = new ItemStack(item, count);
        if (!tag.isEmpty()) stack.setTag(tag);
        return stack;
    })));
}
