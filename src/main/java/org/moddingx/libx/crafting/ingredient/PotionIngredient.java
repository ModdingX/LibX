package org.moddingx.libx.crafting.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An ingredient that checks for a {@link Potion potion} on an item. Unlike {@link EffectIngredient} there
 * must be exactly the potion given here and only registered potions can be used. {@code CustomPotionEffects}
 * is ignored.
 * 
 * Please check whether EffectIngredient matches your needs better as it does in most cases.
 */
public class PotionIngredient extends Ingredient {

    /**
     * The item required for the potion. Can be null t match any item.
     */
    @Nullable
    public final Item potionItem;

    /**
     * The potion type used for this ingredient.
     */
    public final Potion potion;

    public PotionIngredient(@Nullable Item potionItem, Potion potion) {
        super(Stream.empty());
        this.potionItem = potionItem;
        this.potion = potion;
    }

    @Nonnull
    @Override
    public ItemStack[] getItems() {
        ItemStack stack = new ItemStack(this.potionItem == null ? Items.POTION : this.potionItem);
        PotionUtils.setPotion(stack, this.potion);
        return new ItemStack[] { stack };
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack != null && !stack.isEmpty() && (this.potionItem == null || stack.getItem() == this.potionItem)) {
            Potion itemPotion = PotionUtils.getPotion(stack);
            return itemPotion == this.potion;
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
        return PotionIngredient.Serializer.INSTANCE;
    }

    @Override
    public boolean isEmpty() {
        return this.potionItem == Items.AIR;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(PotionIngredient.Serializer.INSTANCE).toString());
        if (this.potionItem == null) {
            json.add("item", JsonNull.INSTANCE);
        } else {
            json.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.potionItem)).toString());
        }
        json.addProperty("potion", Objects.requireNonNull(ForgeRegistries.POTIONS.getKey(this.potion)).toString());
        return json;
    }

    public static class Serializer implements IIngredientSerializer<PotionIngredient> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {

        }

        @Nonnull
        @Override
        public PotionIngredient parse(@Nonnull FriendlyByteBuf buffer) {
            Item potionItem;
            if (buffer.readBoolean()) {
                potionItem = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
                if (potionItem == null) {
                    potionItem = Items.AIR;
                }
            } else {
                potionItem = null;
            }

            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(buffer.readUtf()));
            if (potion == null) {
                potion = Potions.EMPTY;
            }

            return new PotionIngredient(potionItem, potion);
        }

        @Nonnull
        @Override
        public PotionIngredient parse(JsonObject json) {
            JsonElement itemJson = json.get("item");
            Item potionItem;
            if (itemJson.isJsonNull()) {
                potionItem = null;
            } else {
                ResourceLocation potionRl = ResourceLocation.tryParse(itemJson.getAsString());
                potionItem = potionRl == null ? null : ForgeRegistries.ITEMS.getValue(potionRl);
                if (potionItem == null) potionItem = Items.AIR;
            }
            
            Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(json.get("potion").getAsString()));
            if (potion == null) {
                potion = Potions.EMPTY;
            }

            return new PotionIngredient(potionItem, potion);
        }

        @Override
        public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull PotionIngredient ingredient) {
            buffer.writeBoolean(ingredient.potionItem != null);
            if (ingredient.potionItem != null) buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(ingredient.potionItem)));
            buffer.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.POTIONS.getKey(ingredient.potion)));
        }
    }
}
