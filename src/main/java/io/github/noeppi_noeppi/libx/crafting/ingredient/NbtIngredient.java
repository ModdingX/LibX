package io.github.noeppi_noeppi.libx.crafting.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * An ingredient that unlike the one by forge does not check the share tag but the actual
 * ItemStack tag.
 */
public class NbtIngredient extends Ingredient {

    private final ItemStack stack;
    private final ItemStack[] matchingStacks;

    /**
     * If this is true, the tags must be equals. If it is false the matched item must at
     * least have all the tags from this ingredient but may specify more.
     */
    public final boolean exactMatch;

    public NbtIngredient(ItemStack stack) {
        this(stack, false);
    }

    public NbtIngredient(ItemStack stack, boolean exactMatch) {
        super(Stream.of(new Ingredient.SingleItemList(stack)));
        this.stack = stack.copy();
        this.exactMatch = exactMatch;
        this.matchingStacks = new ItemStack[] { stack.copy() };
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null || this.stack.isEmpty() || input.getItem() != this.stack.getItem())
            return false;
        if (input.isDamageable() != this.stack.isDamageable() || (this.stack.isDamageable() && input.getDamage() != this.stack.getDamage())) {
            return false;
        }

        CompoundNBT nbt = this.stack.getTag();
        if (nbt == null) {
            nbt = new CompoundNBT();
        }

        CompoundNBT inputNbt = input.getTag();
        if (inputNbt == null) {
            inputNbt = new CompoundNBT();
        }

        if (this.exactMatch) {
            return inputNbt.equals(nbt);
        } else {
            CompoundNBT merged = inputNbt.copy();
            merged = merged.merge(nbt);

            return merged.equals(inputNbt);
        }
    }

    @Nonnull
    @Override
    public ItemStack[] getMatchingStacks() {
        return this.matchingStacks;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return NbtIngredient.Serializer.INSTANCE;
    }

    @Nonnull
    @Override
    @SuppressWarnings("ConstantConditions")
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(NbtIngredient.Serializer.INSTANCE).toString());
        json.addProperty("item", this.stack.getItem().getRegistryName().toString());
        if (this.stack.hasTag())
            json.addProperty("nbt", this.stack.getTag().toString());
        json.addProperty("exactMatch", this.exactMatch);
        return json;
    }

    public static class Serializer implements IIngredientSerializer<NbtIngredient> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {

        }

        @Nonnull
        @Override
        public NbtIngredient parse(PacketBuffer buffer) {
            return new NbtIngredient(buffer.readItemStack(), buffer.readBoolean());
        }

        @Nonnull
        @Override
        public NbtIngredient parse(@Nonnull JsonObject json) {
            ItemStack stack = CraftingHelper.getItemStack(json, true);
            boolean exactMatch = false;
            if (json.has("exactMatch")) {
                exactMatch = json.get("exactMatch").getAsBoolean();
            }

            return new NbtIngredient(stack, exactMatch);
        }

        @Override
        public void write(PacketBuffer buffer, NbtIngredient ingredient) {
            buffer.writeItemStack(ingredient.stack);
            buffer.writeBoolean(ingredient.exactMatch);
        }
    }
}
