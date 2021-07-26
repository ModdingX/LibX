package io.github.noeppi_noeppi.libx.crafting.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An ingredient that checks for {@link MobEffectInstance potion effects} to be present on a potion. This does
 * not check for a {@link Potion potion} but for the effects. So potions with the {@code CustomPotionEffects} nbt
 * tag will also be detected. And you can match a for example a potion of the turtle master and a potion of
 * slowness as both have the slowness effect.
 */
public class EffectIngredient extends Ingredient {

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
        this(potionStack.getItem(), PotionUtils.getMobEffects(potionStack), false, true, true);
    }

    public EffectIngredient(ItemStack potionStack, boolean extraEffects, boolean higherAmplifier, boolean higherDuration) {
        this(potionStack.getItem(), PotionUtils.getMobEffects(potionStack), extraEffects, higherAmplifier, higherDuration);
    }

    public EffectIngredient(@Nullable Item potionItem, List<MobEffectInstance> effects) {
        this(potionItem, effects, false, true, true);
    }

    public EffectIngredient(@Nullable Item potionItem, List<MobEffectInstance> effects, boolean extraEffects, boolean higherAmplifier, boolean higherDuration) {
        super(Stream.empty());
        this.potionItem = potionItem;
        this.effects = ImmutableList.copyOf(effects);
        this.extraEffects = extraEffects;
        this.higherAmplifier = higherAmplifier;
        this.higherDuration = higherDuration;
    }

    @Nonnull
    @Override
    public ItemStack[] getItems() {
        ItemStack potion = new ItemStack(this.potionItem == null ? Items.POTION : this.potionItem);
        PotionUtils.setCustomEffects(potion, this.effects);
        return new ItemStack[]{potion};
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack != null && !stack.isEmpty() && (this.potionItem == null || stack.getItem() == this.potionItem)) {
            List<MobEffectInstance> effectsLeft = new ArrayList<>(PotionUtils.getMobEffects(stack));
            for (MobEffectInstance effect : this.effects) {
                if (!effectsLeft.removeIf(left -> (left.getEffect() == effect.getEffect())
                        && (left.getAmplifier() == effect.getAmplifier() || (this.higherAmplifier && left.getAmplifier() > effect.getAmplifier()))
                        && (left.getEffect().isInstantenous() || left.getDuration() == effect.getDuration() || (this.higherDuration && left.getDuration() > effect.getDuration())))) {
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
    public IntList getStackingIds() {
        ItemStack[] stacks = this.getItems();
        IntArrayList ial = new IntArrayList(stacks.length);
        for (ItemStack stack : stacks)
            ial.add(StackedContents.getStackingIndex(stack));
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
    public boolean isEmpty() {
        return this.potionItem == Items.AIR || this.potionItem == null && this.effects.isEmpty();
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(Serializer.INSTANCE).toString());
        if (this.potionItem == null) {
            json.add("item", JsonNull.INSTANCE);
        } else {
            json.addProperty("item", this.potionItem.getRegistryName().toString());
        }
        JsonArray jsonEffects = new JsonArray();
        for (MobEffectInstance effect : this.effects) {
            JsonObject effectJson = new JsonObject();
            effectJson.addProperty("potion", effect.getEffect().getRegistryName().toString());
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
        public EffectIngredient parse(@Nonnull FriendlyByteBuf buffer) {
            Item potionItem;
            if (buffer.readBoolean()) {
                potionItem = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
                if (potionItem == null) {
                    potionItem = Items.AIR;
                }
            } else {
                potionItem = null;
            }

            List<MobEffectInstance> effects = new ArrayList<>();
            int effectsSize = buffer.readInt();
            for (int i = 0; i < effectsSize; i++) {
                MobEffect potion = ForgeRegistries.POTIONS.getValue(buffer.readResourceLocation());
                if (potion == null) {
                    potion = MobEffects.MOVEMENT_SPEED;
                }
                int amplifier = buffer.readInt();
                int duration = buffer.readInt();
                effects.add(new MobEffectInstance(potion, duration, amplifier));
            }

            boolean extraEffects = buffer.readBoolean();
            boolean higherAmplifier = buffer.readBoolean();
            boolean higherDuration = buffer.readBoolean();

            return new EffectIngredient(potionItem, effects, extraEffects, higherAmplifier, higherDuration);
        }

        @Nonnull
        @Override
        public EffectIngredient parse(JsonObject json) {
            JsonElement itemJson = json.get("item");
            Item potionItem;
            if (itemJson.isJsonNull()) {
                potionItem = null;
            } else {
                ResourceLocation potionRl = ResourceLocation.tryParse(itemJson.getAsString());
                potionItem = potionRl == null ? null : ForgeRegistries.ITEMS.getValue(potionRl);
                if (potionItem == null) potionItem = Items.AIR;
            }

            List<MobEffectInstance> effects = new ArrayList<>();
            for (JsonElement effectJson : json.get("effects").getAsJsonArray()) {
                JsonObject effect = effectJson.getAsJsonObject();
                MobEffect potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(effect.get("potion").getAsString()));
                if (potion == null) {
                    potion = MobEffects.MOVEMENT_SPEED;
                }
                int amplifier = effect.get("amplifier").getAsInt();
                int duration = effect.get("duration").getAsInt();
                effects.add(new MobEffectInstance(potion, duration, amplifier));
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
        public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull EffectIngredient ingredient) {
            buffer.writeBoolean(ingredient.potionItem != null);
            if (ingredient.potionItem != null) buffer.writeResourceLocation(ingredient.potionItem.getRegistryName());
            buffer.writeInt(ingredient.effects.size());
            for (MobEffectInstance effect : ingredient.effects) {
                buffer.writeResourceLocation(effect.getEffect().getRegistryName());
                buffer.writeInt(effect.getAmplifier());
                buffer.writeInt(effect.getDuration());
            }
            buffer.writeBoolean(ingredient.extraEffects);
            buffer.writeBoolean(ingredient.higherAmplifier);
            buffer.writeBoolean(ingredient.higherDuration);
        }
    }
}
