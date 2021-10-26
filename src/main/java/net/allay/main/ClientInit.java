package net.allay.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ClientInit implements ClientModInitializer {
    public static final EntityModelLayer MODEL_RYU_LAYER = new EntityModelLayer(new Identifier("allay", "ryu"), "main");
    public static final EntityModelLayer MODEL_ALLAY_LAYER = new EntityModelLayer(new Identifier("allay", "allay"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(MainInit.RYU, (context) -> {
            return new RyuRenderer(context);
        });
        EntityModelLayerRegistry.registerModelLayer(MODEL_RYU_LAYER, RyuModel::getTexturedModelData);

        EntityRendererRegistry.register(MainInit.ALLAY, (context) -> {
            return new AllayRenderer(context);
        });
        EntityModelLayerRegistry.registerModelLayer(MODEL_ALLAY_LAYER, AllayModel::getTexturedModelData);
    }
}
