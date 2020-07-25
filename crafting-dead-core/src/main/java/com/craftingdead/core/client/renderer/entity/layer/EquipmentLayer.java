package com.craftingdead.core.client.renderer.entity.layer;

import java.util.function.Consumer;
import org.apache.commons.lang3.Validate;
import com.craftingdead.core.capability.ModCapabilities;
import com.craftingdead.core.client.util.RenderUtil;
import com.craftingdead.core.inventory.InventorySlotType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Layer that renders {@link IEquipableModel}s attached to a player's body.
 */
public class EquipmentLayer<T extends LivingEntity, M extends BipedModel<T>>
    extends LayerRenderer<T, M> {

  private final InventorySlotType slot;

  /**
   * Whether this model should be rotated when the player is crouching.
   */
  private final boolean useCrouchingOrientation;

  /**
   * Whether this model should be rotated accordingly to the player's head.
   */
  private final boolean useHeadOrientation;

  /**
   * Optional arbitrary transformation right before rendering the {@link ItemStack}.
   */
  private final Consumer<MatrixStack> transformation;

  private EquipmentLayer(Builder<T, M> builder) {
    super(builder.entityRenderer);
    this.slot = builder.slot;
    this.useCrouchingOrientation = builder.useCrouchingOrientation;
    this.transformation = builder.tranformation;
    this.useHeadOrientation = builder.useHeadOrientation;
  }

  @Override
  public void render(MatrixStack matrix, IRenderTypeBuffer buffers,
      int packedLight, LivingEntity entity, float p_225628_5_,
      float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_,
      float p_225628_10_) {

    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    entity.getCapability(ModCapabilities.LIVING).ifPresent(living -> {

      ItemStack itemStack = living.getItemHandler().getStackInSlot(this.slot.getIndex());

      if (!itemStack.isEmpty()) {
        IBakedModel itemModel =
            itemRenderer.getItemModelWithOverrides(itemStack, entity.world, entity);


        matrix.push();

        // Applies crouching rotation is needed
        if (this.useCrouchingOrientation && entity.isCrouching()) {
          RenderUtil.applyPlayerCrouchRotation(matrix);
        }

        // Applies the head orientation if needed
        if (this.useHeadOrientation) {
          // Vanilla's transformation for child entities, like baby zombies
          if (entity.isChild()) {
            matrix.translate(0.0D, 0.03125D, 0.0D);
            matrix.scale(0.7F, 0.7F, 0.7F);
            matrix.translate(0.0D, 1.0D, 0.0D);
          }

          this.getEntityModel().func_205072_a().rotate(matrix);
        }

        // Applies the arbitrary transformation if needed
        if (this.transformation != null) {
          this.transformation.accept(matrix);
        }

        // Renders the item. Also note the TransformType.
        itemRenderer
            .renderItem(itemStack, ItemCameraTransforms.TransformType.HEAD, false, matrix, buffers,
                packedLight, OverlayTexture.DEFAULT_UV, itemModel);

        matrix.pop();
      }
    });
  }

  public static class Builder<T extends LivingEntity, M extends BipedModel<T>> {
    private LivingRenderer<T, M> entityRenderer;
    private InventorySlotType slot;
    private Consumer<MatrixStack> tranformation;
    private boolean useCrouchingOrientation;
    private boolean useHeadOrientation;

    public Builder<T, M> withRenderer(LivingRenderer<T, M> entityRenderer) {
      this.entityRenderer = entityRenderer;
      return this;
    }

    public Builder<T, M> withSlot(InventorySlotType slot) {
      this.slot = slot;
      return this;
    }

    public Builder<T, M> withArbitraryTransformation(Consumer<MatrixStack> transformation) {
      this.tranformation = transformation;
      return this;
    }

    public Builder<T, M> withCrouchingOrientation(boolean useCrouchingOrientation) {
      this.useCrouchingOrientation = useCrouchingOrientation;
      return this;
    }

    public Builder<T, M> withHeadOrientation(boolean useHeadOrientation) {
      this.useHeadOrientation = useHeadOrientation;
      return this;
    }

    public EquipmentLayer<T, M> build() {
      Validate.notNull(this.entityRenderer, "The renderer must not be null");
      Validate.notNull(this.slot, "The slot must not be null");
      return new EquipmentLayer<>(this);
    }
  }
}