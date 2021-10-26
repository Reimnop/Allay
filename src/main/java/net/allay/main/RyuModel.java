package net.allay.main;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class RyuModel extends EntityModel<Ryu> {
    private final ModelPart base;
    private final ModelPart left_hand;
    private final ModelPart right_hand;

    public RyuModel(ModelPart modelPart) {
        base = modelPart.getChild("base");
        left_hand = modelPart.getChild("left_hand");
        right_hand = modelPart.getChild("right_hand");
    }

    @Override
    public void setAngles(Ryu entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        boolean bl = entity.getRoll() > 4;

        float k = 1.0F;
        if (bl) {
            k = (float)entity.getVelocity().lengthSquared();
            k /= 0.2F;
            k *= k * k;
        }

        if (k < 1.0F) {
            k = 1.0F;
        }

        right_hand.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 2.0F * limbDistance * 0.5F / k;
        left_hand.pitch = MathHelper.cos(limbAngle * 0.6662F) * 2.0F * limbDistance * 0.5F / k;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        base.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        left_hand.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        right_hand.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartBuilder base_builder = ModelPartBuilder.create();
        base_builder.uv(6, 0).cuboid(-2.0F, -6.0F, -9.0F, 4.0F, 1.0F, 1.0F, false);
        base_builder.uv(0, 0).cuboid(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, false);
        base_builder.uv(0, 0).cuboid(2.0F, -12.0F, -9.0F, 2.0F, 4.0F, 1.0F, false);
        base_builder.uv(0, 0).cuboid(-4.0F, -12.0F, -9.0F, 2.0F, 4.0F, 1.0F, false);
        ModelPartData base = root.addChild("base", base_builder, ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartBuilder left_arm_builder = ModelPartBuilder.create();
        left_arm_builder.uv(0, 5).cuboid(2.0F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, false);
        ModelPartData left_hand = root.addChild("left_hand", left_arm_builder, ModelTransform.pivot(8.0F, 14.0F, 0.0F));

        ModelPartBuilder right_hand_builder = ModelPartBuilder.create();
        right_hand_builder.uv(0, 5).cuboid(-7.0F, 4.0F, -2.0F, 4.0F, 4.0F, 4.0F, false);
        ModelPartData right_hand = root.addChild("right_hand", right_hand_builder, ModelTransform.pivot(-8.0F, 14.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }
}
