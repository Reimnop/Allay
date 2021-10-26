package net.allay.main;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class Ryu extends PathAwareEntity {
    protected Ryu(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }
}
