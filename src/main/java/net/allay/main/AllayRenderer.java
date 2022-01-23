package net.allay.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class AllayRenderer extends MobEntityRenderer<AllayEntity, AllayModel> {

    public AllayRenderer(EntityRendererFactory.Context context) {
        super(context, new AllayModel(context.getPart(ClientInit.MODEL_ALLAY_LAYER)), 0.15625f);
        addFeature(new AllayItemFeatureRenderer(this));
    }

    @Override
    public Identifier getTexture(AllayEntity entity) {
        return new Identifier("allay", "textures/entity/allay.png");
    }

    public class AllayItemFeatureRenderer<T extends LivingEntity, M extends EntityModel<T> & ModelWithArms> extends FeatureRenderer<T, M> {
        public AllayItemFeatureRenderer(FeatureRendererContext<T, M> featureRendererContext) {
            super(featureRendererContext);
        }

        public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
            ItemStack stack = livingEntity.getMainHandStack();
            if (!stack.isEmpty()) {
                matrixStack.push();
                matrixStack.translate(0.0d, 1.45d, -0.195d);
                matrixStack.scale(0.5f, 0.5f, 0.5f);
                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-150.0F));
                matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
                MinecraftClient.getInstance().getHeldItemRenderer().renderItem(livingEntity, stack, ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND, false, matrixStack, vertexConsumerProvider, i);
                matrixStack.pop();
            }
        }
    }
}
