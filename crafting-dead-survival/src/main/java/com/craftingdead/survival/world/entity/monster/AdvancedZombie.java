/*
 * Crafting Dead
 * Copyright (C) 2021  NexusNode LTD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.craftingdead.survival.world.entity.monster;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import com.craftingdead.core.world.entity.ai.FollowAttractiveGrenadeGoal;
import com.craftingdead.core.world.entity.ai.LookAtEntityGoal;
import com.craftingdead.core.world.entity.extension.LivingExtension;
import com.craftingdead.core.world.entity.grenade.FlashGrenadeEntity;
import com.craftingdead.core.world.inventory.ModEquipmentSlotType;
import com.craftingdead.core.world.item.ClothingItem;
import com.craftingdead.core.world.item.HatItem;
import com.craftingdead.core.world.item.MeleeWeaponItem;
import com.craftingdead.core.world.item.ModItems;
import com.craftingdead.core.world.item.gun.Gun;
import com.craftingdead.survival.world.entity.SurvivalPlayerHandler;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.registries.RegistryObject;

public class AdvancedZombie extends Zombie implements RangedAttackMob {

  private static final UUID DAMAGE_MODIFIER_BABY_UUID =
      UUID.fromString("53405062-b8d8-461c-a542-26b0be8ed481");
  private static final AttributeModifier DAMAGE_MODIFIER_BABY =
      new AttributeModifier(DAMAGE_MODIFIER_BABY_UUID, "Baby damage reduction", -1.5D,
          AttributeModifier.Operation.MULTIPLY_BASE);

  private static final UUID HEALTH_MODIFIER_BABY_UUID =
      UUID.fromString("69d754ea-1ae3-4684-bb69-51a29de92b9a");
  private static final AttributeModifier HEALTH_MODIFIER_BABY =
      new AttributeModifier(HEALTH_MODIFIER_BABY_UUID, "Baby health reduction", -1.5D,
          AttributeModifier.Operation.MULTIPLY_BASE);

  private static final float MELEE_CHANCE = 0.15F;
  private static final float CLOTHING_CHANCE = 0.25F;
  private static final float HAT_CHANCE = 0.05F;

  private static final EntityDataAccessor<Integer> TEXTURE_NUMBER =
      SynchedEntityData.defineId(AdvancedZombie.class, EntityDataSerializers.INT);

  private RangedAttackGoal rangedAttackGoal;

  private long triggerPressedStartTime;

  public AdvancedZombie(EntityType<? extends AdvancedZombie> type, Level world) {
    super(type, world);
  }

  @Override
  protected void registerGoals() {
    super.registerGoals();
    this.rangedAttackGoal = new RangedAttackGoal(this, 1.0D, 40, 20F) {
      @Override
      public boolean canUse() {
        return super.canUse() && AdvancedZombie.this.getMainHandItem()
            .getCapability(Gun.CAPABILITY).isPresent();
      }
    };
    this.goalSelector.addGoal(2, this.rangedAttackGoal);
    this.goalSelector.addGoal(1, new FollowAttractiveGrenadeGoal(this, 1.15F));
    this.goalSelector.addGoal(4,
        new LookAtEntityGoal<>(this, FlashGrenadeEntity.class, 20.0F, 0.35F));
  }

  @Override
  protected void addBehaviourGoals() {
    super.addBehaviourGoals();
    this.targetSelector.addGoal(2,
        new NearestAttackableTargetGoal<Player>(this, Player.class, 5, false, false,
            targetEntity -> targetEntity.getCapability(LivingExtension.CAPABILITY)
                .resolve()
                .flatMap(extension -> extension.getHandler(SurvivalPlayerHandler.ID))
                .map(SurvivalPlayerHandler.class::cast)
                .map(handler -> handler.getSoundLevel() >= targetEntity.distanceTo(this))
                .orElse(false)) {
          @Override
          public double getFollowDistance() {
            return 100.0D;
          }
        });
  }

  @Override
  public void setBaby(boolean baby) {
    super.setBaby(baby);
    if (this.level != null && !this.level.isClientSide) {
      AttributeInstance damageAttribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
      damageAttribute.removeModifier(DAMAGE_MODIFIER_BABY);
      damageAttribute.removeModifier(HEALTH_MODIFIER_BABY);
      if (baby) {
        damageAttribute.addTransientModifier(DAMAGE_MODIFIER_BABY);
        damageAttribute.addTransientModifier(HEALTH_MODIFIER_BABY);
      }
    }
  }

  @Override
  public boolean checkSpawnRules(LevelAccessor world, MobSpawnType spawnReason) {
    return true;
  }

  @Override
  public void defineSynchedData() {
    super.defineSynchedData();
    this.getEntityData().define(TEXTURE_NUMBER, 0);
  }

  @Override
  public int getArmorValue() {
    int armorValue = super.getArmorValue() + 2;
    if (armorValue > 20) {
      armorValue = 20;
    }
    return armorValue;
  }

  @Override
  public int getMaxSpawnClusterSize() {
    return 12;
  }

  @Override
  protected boolean isSunSensitive() {
    return false;
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compound) {
    super.readAdditionalSaveData(compound);
    this.entityData.set(TEXTURE_NUMBER, compound.getInt("textureNumber"));
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compound) {
    super.addAdditionalSaveData(compound);
    compound.putInt("textureNumber", this.entityData.get(TEXTURE_NUMBER));
  }

  @Override
  protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {
    this.entityData.set(TEXTURE_NUMBER, this.random.nextInt(23));
    this.setItemSlot(EquipmentSlot.MAINHAND, this.getHeldStack());
    this.getCapability(LivingExtension.CAPABILITY).ifPresent(living -> {
      living.getItemHandler().setStackInSlot(ModEquipmentSlotType.CLOTHING.getIndex(),
          this.getClothingStack());
      living.getItemHandler().setStackInSlot(ModEquipmentSlotType.HAT.getIndex(),
          this.getHatStack());
    });
  }

  protected ItemStack getHeldStack() {
    return this.getRandomItem(item -> item instanceof MeleeWeaponItem, MELEE_CHANCE)
        .map(Item::getDefaultInstance)
        .orElse(ItemStack.EMPTY);
  }

  protected ItemStack getClothingStack() {
    return this.getRandomItem(item -> item instanceof ClothingItem, CLOTHING_CHANCE)
        .map(Item::getDefaultInstance)
        .orElse(ItemStack.EMPTY);
  }

  protected ItemStack getHatStack() {
    return this.getRandomItem(item -> item instanceof HatItem, HAT_CHANCE)
        .map(Item::getDefaultInstance)
        .orElse(ItemStack.EMPTY);
  }

  protected Optional<Item> getRandomItem(Predicate<Item> predicate, float probability) {
    if (this.random.nextFloat() < probability) {
      var items = ModItems.ITEMS
          .getEntries()
          .stream()
          .map(RegistryObject::get)
          .filter(predicate)
          .toList();
      return Optional.of(items.get(this.random.nextInt(items.size())));
    }
    return Optional.empty();
  }

  public int getTextureNumber() {
    return this.entityData.get(TEXTURE_NUMBER);
  }

  public static boolean checkAdvancedZombieSpawnRules(
      EntityType<? extends AdvancedZombie> entityType, LevelAccessor level,
      MobSpawnType reason, BlockPos blockPos, Random random) {
    return level.getBrightness(LightLayer.BLOCK, blockPos) <= 8
        && checkAnyLightMonsterSpawnRules(entityType, level, reason, blockPos, random);
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.level.isClientSide()) {
      this.getCapability(LivingExtension.CAPABILITY)
          .ifPresent(living -> living.getMainHandGun().ifPresent(gun -> {
            if (gun.isTriggerPressed()
                && (!this.rangedAttackGoal.canContinueToUse() || (Util.getMillis()
                    - this.triggerPressedStartTime > 1000 + this.random.nextInt(2000)))) {
              gun.setTriggerPressed(living, false, true);
            }
          }));
    }
  }

  @Override
  public void performRangedAttack(LivingEntity livingEntity, float distance) {
    if (!this.level.isClientSide()) {
      this.getCapability(LivingExtension.CAPABILITY)
          .ifPresent(living -> living.getMainHandGun().ifPresent(gun -> {
            this.triggerPressedStartTime = Util.getMillis();
            gun.setTriggerPressed(living, true, true);
          }));
    }
  }
}