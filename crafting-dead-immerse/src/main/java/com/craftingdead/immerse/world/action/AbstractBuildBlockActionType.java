/*
 * Crafting Dead
 * Copyright (C) 2022  NexusNode LTD
 *
 * This Non-Commercial Software License Agreement (the "Agreement") is made between you (the "Licensee") and NEXUSNODE (BRAD HUNTER). (the "Licensor").
 * By installing or otherwise using Crafting Dead (the "Software"), you agree to be bound by the terms and conditions of this Agreement as may be revised from time to time at Licensor's sole discretion.
 *
 * If you do not agree to the terms and conditions of this Agreement do not download, copy, reproduce or otherwise use any of the source code available online at any time.
 *
 * https://github.com/nexusnode/crafting-dead/blob/1.18.x/LICENSE.txt
 *
 * https://craftingdead.net/terms.php
 */

package com.craftingdead.immerse.world.action;

import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;

public abstract class AbstractBuildBlockActionType extends BuildActionType {

  private final Supplier<Block> block;

  protected AbstractBuildBlockActionType(Builder<?> builder) {
    super(builder);
    this.block = builder.block;
  }

  public Block getBlock() {
    return this.block.get();
  }

  public static abstract class Builder<SELF extends Builder<SELF>>
      extends BuildActionType.Builder<SELF> {

    private Supplier<Block> block;

    public SELF block(Block block) {
      return this.block(() -> block);
    }

    public SELF block(Supplier<Block> block) {
      this.block = block;
      return this.self();
    }
  }
}
