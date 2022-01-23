package net.allay.main;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainInit implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("allay");

	public static final PeeSeaItem PEE_SEA_ITEM = new PeeSeaItem(new FabricItemSettings().group(ItemGroup.MISC));
	public static final EntityType<Ryu> RYU = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("allay", "ryu"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Ryu::new).dimensions(EntityDimensions.fixed(1.0f, 1.0f)).build()
	);
	public static final Item RYU_SPAWN_EGG = new SpawnEggItem(RYU, 11363325, 14595579, new Item.Settings().group(ItemGroup.MISC));
	public static final EntityType<AllayEntity> ALLAY = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("allay", "allay"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, AllayEntity::new).dimensions(EntityDimensions.fixed(0.3125f, 0.625f)).build()
	);
	public static final Item ALLAY_SPAWN_EGG = new SpawnEggItem(ALLAY, 897260, 4125951, new Item.Settings().group(ItemGroup.MISC));

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("allay", "pee_sea"), PEE_SEA_ITEM);
		FabricDefaultAttributeRegistry.register(RYU, Ryu.createMobAttributes());
		Registry.register(Registry.ITEM, new Identifier("allay", "ryu_spawn_egg"), RYU_SPAWN_EGG);
		FabricDefaultAttributeRegistry.register(ALLAY, AllayEntity.createAllayAttributes());
		Registry.register(Registry.ITEM, new Identifier("allay", "allay_spawn_egg"), ALLAY_SPAWN_EGG);
	}
}
