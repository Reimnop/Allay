package net.allay.main;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class RyuRenderer extends MobEntityRenderer<Ryu, RyuModel> {

    public RyuRenderer(EntityRendererFactory.Context context) {
        super(context, new RyuModel(context.getPart(ClientInit.MODEL_RYU_LAYER)), 0.625f);
    }

    @Override
    public Identifier getTexture(Ryu entity) {
        return new Identifier("allay", "textures/entity/ryu.png");
    }
}
