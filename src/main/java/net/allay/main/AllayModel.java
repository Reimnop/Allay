package net.allay.main;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

public class AllayModel extends EntityModel<Allay> implements ModelWithArms, ModelWithHead {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_arm;
    private final ModelPart right_arm;

    public AllayModel(ModelPart modelPart) {
        body = modelPart.getChild("body");
        head = modelPart.getChild("head");
        left_arm = modelPart.getChild("left_arm");
        right_arm = modelPart.getChild("right_arm");
    }

    @Override
    public void setAngles(Allay entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        ModelPart left_wing = head.getChild("left_wing");
        ModelPart right_wing = head.getChild("right_wing");

        left_wing.yaw = (MathHelper.sin(animationProgress * 0.8f) - 0.5f) * MathHelper.PI * 0.125f;
        right_wing.yaw = -left_wing.yaw;

        body.pitch = MathHelper.sin(animationProgress * 0.25f) * MathHelper.PI * 0.0125f;

        boolean bl = entity.getRoll() > 4;

        head.yaw = headYaw * 0.017453292F;
        if (bl) {
            head.pitch = -0.7853982F;
        } else {
            head.pitch = headPitch * 0.017453292F;
        }

        body.yaw = 0.0F;
        float k = 1.0F;
        if (bl) {
            k = (float)entity.getVelocity().lengthSquared();
            k /= 0.2F;
            k *= k * k;
        }

        if (k < 1.0F) {
            k = 1.0F;
        }

        if (!entity.getMainHandStack().isEmpty()) {
            right_arm.pitch = -1.13446401f;
            left_arm.pitch = -1.13446401f; // -65 degrees
        } else {
            right_arm.pitch = MathHelper.cos(limbAngle * 0.85f + 3.1415927F) * 2.0F * limbDistance * 0.5F / k;
            left_arm.pitch = MathHelper.cos(limbAngle * 0.85f) * 2.0F * limbDistance * 0.5F / k;
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        head.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        left_arm.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        right_arm.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartBuilder body_builder = ModelPartBuilder.create();

        ModelPartData body = root.addChild("body", body_builder, ModelTransform.pivot(0.0F, 19.0F, 0.0F));

        ModelPartBuilder body_r1_builder = ModelPartBuilder.create();
        body_r1_builder.uv(20, 0).cuboid(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F, false);
        ModelPartData body_r1 = body.addChild("body_r1", body_r1_builder, ModelTransform.of(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        ModelPartBuilder head_builder = ModelPartBuilder.create();
        head_builder.uv(0, 0).cuboid(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, false);
        ModelPartData head = root.addChild("head", head_builder, ModelTransform.pivot(0.0F, 19.0F, 0.0F));

        ModelPartBuilder left_arm_builder = ModelPartBuilder.create();
        left_arm_builder.uv(0, 0).cuboid(0.0F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, false);
        ModelPartData left_arm = root.addChild("left_arm", left_arm_builder, ModelTransform.pivot(1.5F, 19.866F, 0.5F));

        ModelPartBuilder right_arm_builder = ModelPartBuilder.create();
        right_arm_builder.uv(0, 0).cuboid(-1.0F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, true);
        ModelPartData right_arm = root.addChild("right_arm", right_arm_builder, ModelTransform.pivot(-1.5F, 19.866F, 0.5F));

        ModelPartBuilder left_wing_builder = ModelPartBuilder.create();
        left_wing_builder.uv(0, 10).cuboid(0.0F, -3.0F, 0.0F, 6.0F, 5.0F, 1.0F, false);
        ModelPartData left_wing = head.addChild("left_wing", left_wing_builder, ModelTransform.pivot(1.5F, 0.0F, 2.5F));

        ModelPartBuilder right_wing_builder = ModelPartBuilder.create();
        right_wing_builder.uv(0, 10).cuboid(-6.0F, -3.0F, 0.0F, 6.0F, 5.0F, 1.0F, true);
        ModelPartData right_wing = head.addChild("right_wing", right_wing_builder, ModelTransform.pivot(-1.5F, 0.0F, 2.5F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        getArm(arm).rotate(matrices);
    }

    private ModelPart getArm(Arm arm) {
        return arm == Arm.LEFT ? left_arm : right_arm;
    }

    @Override
    public ModelPart getHead() {
        return head;
    }
}
