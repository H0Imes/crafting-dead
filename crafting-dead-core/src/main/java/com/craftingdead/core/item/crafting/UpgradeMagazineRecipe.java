package com.craftingdead.core.item.crafting;

import com.craftingdead.core.capability.ModCapabilities;
import com.craftingdead.core.capability.magazine.IMagazine;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class UpgradeMagazineRecipe extends SpecialRecipe {

  public UpgradeMagazineRecipe(ResourceLocation id) {
    super(id);
  }

  @Override
  public boolean matches(CraftingInventory inventory, World world) {
    ItemStack magazineStack = ItemStack.EMPTY;
    int ironNuggetCount = 0;
    for (int i = 0; i < inventory.getSizeInventory(); ++i) {
      ItemStack itemStack = inventory.getStackInSlot(i);
      boolean isSingleMagazine = itemStack.getCapability(ModCapabilities.MAGAZINE).isPresent()
          && itemStack.getCount() == 1 && i == (inventory.getSizeInventory()) / 2;
      boolean isIronNuggets = Tags.Items.NUGGETS_IRON.contains(itemStack.getItem());
      if (isSingleMagazine) {
        if (!magazineStack.isEmpty()) {
          return false;
        } else {
          magazineStack = itemStack;
        }
      } else if (isIronNuggets) {
        ironNuggetCount++;
      } else if (!itemStack.isEmpty()) {
        return false;
      }
    }

    boolean isValidMagazine = magazineStack.getCapability(ModCapabilities.MAGAZINE).isPresent();
    boolean canUpgrade = ironNuggetCount >= 8 && !magazineStack
        .getCapability(ModCapabilities.MAGAZINE)
        .map(IMagazine::getNextTier)
        .map(ItemStack::new)
        .orElse(ItemStack.EMPTY)
        .isEmpty();
    return isValidMagazine && canUpgrade;
  }

  @Override
  public ItemStack getCraftingResult(CraftingInventory inventory) {
    ItemStack magazineStack = ItemStack.EMPTY;
    int ironNuggetCount = 0;
    for (int i = 0; i < inventory.getSizeInventory(); ++i) {
      ItemStack itemStack = inventory.getStackInSlot(i);
      if (itemStack.getCapability(ModCapabilities.MAGAZINE).isPresent()) {
        magazineStack = itemStack;
      } else if (Tags.Items.NUGGETS_IRON.contains(itemStack.getItem())) {
        ironNuggetCount++;
      }
    }

    int tiers = ironNuggetCount / 8;

    ItemStack nextTier = magazineStack
        .getCapability(ModCapabilities.MAGAZINE)
        .map(IMagazine::getNextTier)
        .map(ItemStack::new)
        .orElse(ItemStack.EMPTY);
    int oldSize =
        magazineStack.getCapability(ModCapabilities.MAGAZINE).map(IMagazine::getSize).orElse(0);
    while (tiers > 0 && !nextTier.isEmpty()) {
      magazineStack = nextTier;
      magazineStack
          .getCapability(ModCapabilities.MAGAZINE)
          .ifPresent(magazine -> magazine.setSize(oldSize));
      tiers--;
    }

    return magazineStack.copy();
  }

  @Override
  public boolean canFit(int width, int height) {
    return width * height >= 9;
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return ModRecipeSerializers.UPGRADE_MAGAZINE.get();
  }
}