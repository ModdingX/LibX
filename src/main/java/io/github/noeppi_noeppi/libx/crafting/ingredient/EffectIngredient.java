package io.github.noeppi_noeppi.libx.crafting.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An ingredient that checks for potion effects to be present on a potion. This does not check for a
 * potion but for the effects. So potions with the {@code CustomPotionEffects} nbt tag will also be detected.
 * And you can match a for example a potion of the turtle master and a potion of slowness as both have the
 * slowness effect.
 */
public class EffectIngredient extends Ingredient {

    /**
     * The item required for the potion.
     */
    public final Item potionItem;

    /**
     * A list of effects that an ItemStack needs.
     */
    public final List<EffectInstance> effects;

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
        this(potionStack.getItem(), PotionUtils.getEffectsFromStack(potionStack), false, true, true);
    }

    public EffectIngredient(ItemStack potionStack, boolean extraEffects, boolean higherAmplifier, boolean higherDuration) {
        this(potionStack.getItem(), PotionUtils.getEffectsFromStack(potionStack), extraEffects, higherAmplifier, higherDuration);
    }

    public EffectIngredient(Item potionItem, List<EffectInstance> effects) {
        this(potionItem, effects, false, true, true);
    }

    public EffectIngredient(Item potionItem, List<EffectInstance> effects, boolean extraEffects, boolean higherAmplifier, boolean higherDuration) {
        super(Stream.empty());
        this.potionItem = potionItem;
        this.effects = ImmutableList.copyOf(effects);
        this.extraEffects = extraEffects;
        this.higherAmplifier = higherAmplifier;
        this.higherDuration = higherDuration;
    }

    @Nonnull
    @Override
    public ItemStack[] getMatchingStacks() {
        ItemStack potion = new ItemStack(this.potionItem);
        PotionUtils.appendEffects(potion, this.effects);
        return new ItemStack[]{potion};
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != this.potionItem) {
            return false;
        }
        List<EffectInstance> effectsLeft = new ArrayList<>(PotionUtils.getEffectsFromStack(stack));
        for (EffectInstance effect : this.effects) {
            if (!effectsLeft.removeIf(left -> (left.getPotion() == effect.getPotion())
                    && (left.getAmplifier() == effect.getAmplifier() || (this.higherAmplifier && left.getAmplifier() > effect.getAmplifier()))
                    && (left.getPotion().isInstant() || left.getDuration() == effect.getDuration() || (this.higherDuration && left.getDuration() > effect.getDuration())))) {
                return false;
            }
        }
        return effectsLeft.isEmpty() || this.extraEffects;
    }

    @Nonnull
    @Override
    public IntList getValidItemStacksPacked() {
        ItemStack[] stacks = this.getMatchingStacks();
        IntArrayList ial = new IntArrayList(stacks.length);
        for (ItemStack stack : stacks)
            ial.add(RecipeItemHelper.pack(stack));
        return ial;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public boolean hasNoMatchingItems() {
        return this.potionItem == Items.AIR;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(Serializer.INSTANCE).toString());
        json.addProperty("item", this.potionItem.getRegistryName().toString());
        JsonArray jsonEffects = new JsonArray();
        for (EffectInstance effect : this.effects) {
            JsonObject effectJson = new JsonObject();
            effectJson.addProperty("potion", effect.getPotion().getRegistryName().toString());
            effectJson.addProperty("amplifier", effect.getAmplifier());
            effectJson.addProperty("duration", effect.getDuration());
            jsonEffects.add(effectJson);
        }
        json.add("effects", jsonEffects);
        json.addProperty("extraEffects", this.extraEffects);
        json.addProperty("higherAmplifier", this.higherAmplifier);
        json.addProperty("higherDuration", this.higherDuration);
        return json;
    }

    public static class Serializer implements IIngredientSerializer<EffectIngredient> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {

        }

        @Nonnull
        @Override
        public EffectIngredient parse(@Nonnull PacketBuffer buffer) {
            Item potionItem = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
            if (potionItem == null) {
                potionItem = Items.AIR;
            }

            List<EffectInstance> effects = new ArrayList<>();
            int effectsSize = buffer.readInt();
            for (int i = 0; i < effectsSize; i++) {
                Effect potion = ForgeRegistries.POTIONS.getValue(buffer.readResourceLocation());
                if (potion == null) {
                    potion = Effects.SPEED;
                }
                int amplifier = buffer.readInt();
                int duration = buffer.readInt();
                effects.add(new EffectInstance(potion, duration, amplifier));
            }

            boolean extraEffects = buffer.readBoolean();
            boolean higherAmplifier = buffer.readBoolean();
            boolean higherDuration = buffer.readBoolean();

            return new EffectIngredient(potionItem, effects, extraEffects, higherAmplifier, higherDuration);
        }

        @Nonnull
        @Override
        public EffectIngredient parse(JsonObject json) {
            Item potionItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("item").getAsString()));
            if (potionItem == null) {
                potionItem = Items.AIR;
            }

            List<EffectInstance> effects = new ArrayList<>();
            for (JsonElement effectJson : json.get("effects").getAsJsonArray()) {
                JsonObject effect = effectJson.getAsJsonObject();
                Effect potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(effect.get("potion").getAsString()));
                if (potion == null) {
                    potion = Effects.SPEED;
                }
                int amplifier = effect.get("amplifier").getAsInt();
                int duration = effect.get("duration").getAsInt();
                effects.add(new EffectInstance(potion, duration, amplifier));
            }

            boolean extraEffects = false;
            if (json.has("extraEffects")) {
                extraEffects = json.get("extraEffects").getAsBoolean();
            }

            boolean higherAmplifier = true;
            if (json.has("higherAmplifier")) {
                higherAmplifier = json.get("higherAmplifier").getAsBoolean();
            }

            boolean higherDuration = true;
            if (json.has("higherDuration")) {
                higherDuration = json.get("higherDuration").getAsBoolean();
            }

            return new EffectIngredient(potionItem, effects, extraEffects, higherAmplifier, higherDuration);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void write(@Nonnull PacketBuffer buffer, @Nonnull EffectIngredient ingredient) {
            buffer.writeResourceLocation(ingredient.potionItem.getRegistryName());
            buffer.writeInt(ingredient.effects.size());
            for (EffectInstance effect : ingredient.effects) {
                buffer.writeResourceLocation(effect.getPotion().getRegistryName());
                buffer.writeInt(effect.getAmplifier());
                buffer.writeInt(effect.getDuration());
            }
            buffer.writeBoolean(ingredient.extraEffects);
            buffer.writeBoolean(ingredient.higherAmplifier);
            buffer.writeBoolean(ingredient.higherDuration);
        }
    }
}