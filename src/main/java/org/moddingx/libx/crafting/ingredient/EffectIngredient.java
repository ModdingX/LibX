package org.moddingx.libx.crafting.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.apache.commons.lang3.stream.Streams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An ingredient that checks for {@link MobEffectInstance potion effects} to be present on a potion. This does
 * not check for a {@link Potion potion} but for the effects. So potions with the {@code CustomPotionEffects} nbt
 * tag will also be detected. And you can match a for example a potion of the turtle master and a potion of
 * slowness as both have the slowness effect.
 */
public class EffectIngredient implements ICustomIngredient {
    
    public static final MapCodec<EffectIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ei -> Objects.requireNonNullElse(ei.potionItem, Items.AIR)),
                    MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(ei -> ei.effects),
                    Codec.BOOL.fieldOf("allows_extra_effects").orElse(false).forGetter(ei -> ei.extraEffects),
                    Codec.BOOL.fieldOf("allows_higher_amplifier").orElse(true).forGetter(ei -> ei.higherAmplifier),
                    Codec.BOOL.fieldOf("allows_higher_duration").orElse(true).forGetter(ei -> ei.higherDuration)
            ).apply(instance, EffectIngredient::new)
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, EffectIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), ei -> ei.potionItem,
            ByteBufCodecs.<RegistryFriendlyByteBuf, MobEffectInstance>list().apply(MobEffectInstance.STREAM_CODEC), ei -> ei.effects,
            ByteBufCodecs.BOOL, ei -> ei.extraEffects,
            ByteBufCodecs.BOOL, ei -> ei.higherAmplifier,
            ByteBufCodecs.BOOL, ei -> ei.higherDuration,
            EffectIngredient::new
    );
    
    public static final IngredientType<EffectIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    /**
     * The item required for the potion. Can be null t match any item.
     */
    @Nullable
    public final Item potionItem;

    /**
     * A list of effects that an ItemStack needs.
     */
    public final List<MobEffectInstance> effects;

    /**
     * Whether potions with more effects than the ones specified in this ingredient are matched.
     */
    public final boolean extraEffects;

    /**
     * Whether potions with a higher amplifier as specified in this ingredient are matched.
     */
    public final boolean higherAmplifier;

    /**
     * Whether potions with a higher duration as specified in this ingredient are matched.
     */
    public final boolean higherDuration;

    public EffectIngredient(ItemStack potionStack) {
        this(potionStack.getItem(), getEffects(potionStack), false, true, true);
    }

    public EffectIngredient(ItemStack potionStack, boolean extraEffects, boolean higherAmplifier, boolean higherDuration) {
        this(potionStack.getItem(), getEffects(potionStack), extraEffects, higherAmplifier, higherDuration);
    }

    public EffectIngredient(@Nullable Item potionItem, List<MobEffectInstance> effects) {
        this(potionItem, effects, false, true, true);
    }

    public EffectIngredient(@Nullable Item potionItem, List<MobEffectInstance> effects, boolean extraEffects, boolean higherAmplifier, boolean higherDuration) {
        this(potionItem, effects.stream(), extraEffects, higherAmplifier, higherDuration);
    }
    
    private EffectIngredient(@Nullable Item potionItem, Stream<MobEffectInstance> effects, boolean extraEffects, boolean higherAmplifier, boolean higherDuration) {
        this.potionItem = potionItem;
        this.effects = effects.map(MobEffectInstance::new).toList();
        this.extraEffects = extraEffects;
        this.higherAmplifier = higherAmplifier;
        this.higherDuration = higherDuration;
    }

    @Nonnull
    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack != null && !stack.isEmpty() && (this.potionItem == null || stack.getItem() == this.potionItem)) {
            List<MobEffectInstance> effectsLeft = new ArrayList<>(getEffects(stack).toList());
            for (MobEffectInstance effect : this.effects) {
                if (!effectsLeft.removeIf(left -> (left.getEffect() == effect.getEffect())
                        && (left.getAmplifier() == effect.getAmplifier() || (this.higherAmplifier && left.getAmplifier() > effect.getAmplifier()))
                        && (left.getEffect().value().isInstantenous() || left.getDuration() == effect.getDuration() || (this.higherDuration && left.getDuration() > effect.getDuration())))) {
                    return false;
                }
            }
            return effectsLeft.isEmpty() || this.extraEffects;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public Stream<Holder<Item>> items() {
        Item potion = this.potionItem == null ? Items.POTION : this.potionItem;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(potion);
        Optional<Holder.Reference<Item>> optional = BuiltInRegistries.ITEM.get(id);

        return optional.stream().map(itemReference -> itemReference);
    }

    private static Stream<MobEffectInstance> getEffects(ItemStack stack) {
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return Stream.of();
        return Streams.of(potionContents.getAllEffects());
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
