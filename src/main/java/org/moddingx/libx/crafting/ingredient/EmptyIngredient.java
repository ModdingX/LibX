package org.moddingx.libx.crafting.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class EmptyIngredient implements ICustomIngredient {

    public static final MapCodec<EmptyIngredient> CODEC = MapCodec.unit(EmptyIngredient::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, EmptyIngredient> STREAM_CODEC = StreamCodec.of(
            (buf, ingredient) -> {},
            buf -> new EmptyIngredient()
    );

    public static final IngredientType<EmptyIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    @Override
    public boolean test(@Nonnull ItemStack stack) {
        return false;
    }

    @Nonnull
    @Override
    public Stream<Holder<Item>> items() {
        return Stream.empty();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
