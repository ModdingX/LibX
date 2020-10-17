package io.github.noeppi_noeppi.libx.crafting.ingredient;

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
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * An ingredient that checks for a potion on an item. Unlike {@link EffectIngredient} there must
 * be exactly the potion given here and only registered potions can be used. {@code CustomPotionEffects}
 * is ignored.
 * <p>
 * Please check whether EffectIngredient matches your needs better as it does in most cases.
 */
public class PotionIngredient extends Ingredient {

    /**
     * The item required for the potion.
     */
    public final Item potionItem;

    /**
     * The potion type used for this ingredient.
     */
    public final Potion potion;

    public PotionIngredient(Item potionItem, Potion potion) {
        super(Stream.empty());
        this.potionItem = potionItem;
        this.potion = potion;
    }

    @Nonnull
    @Override
    public ItemStack[] getMatchingStacks() {
        ItemStack stack = new ItemStack(this.potionItem);
        PotionUtils.addPotionToItemStack(stack, this.potion);
        return new ItemStack[]{stack};
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != this.potionItem) {
            return false;
        }
        Potion itemPotion = PotionUtils.getPotionFromItem(stack);
        return itemPotion == this.potion;
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
    protected void invalidate() {
        //
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
    public boolean hasNoMatchingItems() {
        return this.potionItem == Items.AIR;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(PotionIngredient.Serializer.INSTANCE).toString());
        json.addProperty("item", this.potionItem.getRegistryName().toString());
        json.addProperty("potion", this.potion.getRegistryName().toString());
        return json;
    }

    public static class Serializer implements IIngredientSerializer<PotionIngredient> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {

        }

        @Nonnull
        @Override
        public PotionIngredient parse(@Nonnull PacketBuffer buffer) {
            Item potionItem = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
            if (potionItem == null) {
                potionItem = Items.AIR;
            }

            Potion potion = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation(buffer.readString()));
            if (potion == null) {
                potion = Potions.EMPTY;
            }

            return new PotionIngredient(potionItem, potion);
        }

        @Nonnull
        @Override
        public PotionIngredient parse(JsonObject json) {
            Item potionItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("item").getAsString()));
            if (potionItem == null) {
                potionItem = Items.AIR;
            }

            Potion potion = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation(json.get("potion").getAsString()));
            if (potion == null) {
                potion = Potions.EMPTY;
            }

            return new PotionIngredient(potionItem, potion);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void write(@Nonnull PacketBuffer buffer, @Nonnull PotionIngredient ingredient) {
            buffer.writeResourceLocation(ingredient.potionItem.getRegistryName());
            buffer.writeResourceLocation(ingredient.potion.getRegistryName());
        }
    }
}
