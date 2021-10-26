package net.allay.main;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.*;

public class Allay extends PathAwareEntity implements Tameable {
    private static final TrackedData<Optional<UUID>> OWNER_UUID;

    private static final byte TAME_SUCCESSFUL = 90;
    private static final byte TAME_FAILED = 91;

    public List<ItemStack> inventory = new ArrayList();

    protected Allay(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        moveControl = new FlightMoveControl(this, 10, true);
        setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0F);
        setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
        setPathfindingPenalty(PathNodeType.WATER, 0.0F);
    }

    static {
        OWNER_UUID = DataTracker.registerData(Allay.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new LookAroundGoal(this));
        goalSelector.add(0, new PickupItemsGoal(this, 16.0D));
        goalSelector.add(1, new FollowOwnerGoal(this));
        goalSelector.add(2, new FlyRandomlyGoal(this, 16.0D));
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d v = getVelocity();
        if (!onGround && v.y < 0.0D) {
            setVelocity(v.multiply(1.0D, 0.6D, 1.0D));
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OWNER_UUID, Optional.empty());
    }

    @Override
    public boolean cannotDespawn() {
        return isTamed();
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.getActiveHand() != hand) {
            return ActionResult.FAIL;
        }

        ItemStack playerStack = player.getStackInHand(hand);
        if (world.isClient()) {
            return !isTamed() && playerStack.getItem() == Items.COOKIE ? ActionResult.CONSUME : ActionResult.PASS;
        } else {
            // taming
            if (!isTamed()) {
                if (playerStack.getItem() == Items.COOKIE) {
                    if (random.nextInt(3) == 0) {
                        world.sendEntityStatus(this, TAME_SUCCESSFUL);
                        setOwner(player);
                    } else {
                        world.sendEntityStatus(this, TAME_FAILED);
                    }
                    if (!player.isCreative()) {
                        playerStack.decrement(1);
                    }
                    return ActionResult.SUCCESS;
                }
            }
            else if (getOwnerUuid() == player.getUuid()) {
                ItemStack allayStack = getMainHandStack();
                if (playerStack.isEmpty() && !allayStack.isEmpty()) {
                    depositItems();
                    return ActionResult.SUCCESS;
                }
                else if (!playerStack.isEmpty() && allayStack.isEmpty()) {
                    ItemStack playerStackCopy = playerStack.copy();
                    playerStackCopy.setCount(1);
                    equipStack(EquipmentSlot.MAINHAND, playerStackCopy);
                    if (!player.isCreative()) {
                        playerStack.decrement(1);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.FAIL;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source == DamageSource.FALL) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public void handleStatus(byte status) {
        super.handleStatus(status);

        switch (status) {
            case TAME_SUCCESSFUL -> showEmoteParticle(true);
            case TAME_FAILED -> showEmoteParticle(false);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!world.isClient && world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && getOwner() instanceof ServerPlayerEntity) {
            getOwner().sendSystemMessage(getDamageTracker().getDeathMessage(), Util.NIL_UUID);
        }

        super.onDeath(source);
    }

    @Override
    public UUID getOwnerUuid() {
        return dataTracker.get(OWNER_UUID).orElse(null);
    }

    @Override
    public PlayerEntity getOwner() {
        return dataTracker.get(OWNER_UUID).map(value -> world.getPlayerByUuid(value)).orElse(null);
    }

    private void setOwner(PlayerEntity player) {
        if (player == null) {
            dataTracker.set(OWNER_UUID, Optional.empty());
        }
        else {
            dataTracker.set(OWNER_UUID, Optional.of(player.getUuid()));
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
            PlayerAdvancementTracker tracker = serverPlayer.getAdvancementTracker();
            Advancement advancement = serverPlayer.getServer().getAdvancementLoader().get(new Identifier("minecraft", "husbandry/tame_an_animal"));
            tracker.grantCriterion(advancement, "tamed_animal");
        }
    }

    private boolean isTamed() {
        return dataTracker.get(OWNER_UUID).isPresent();
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        dataTracker.get(OWNER_UUID).ifPresent(value -> nbt.putUuid("Owner", value));

        if (!inventory.isEmpty()) {
            NbtList list = new NbtList();
            for (int i = 0; i < inventory.size(); i++) {
                list.add(inventory.get(i).writeNbt(new NbtCompound()));
            }
            nbt.put("Inventory", list);
        }
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Owner")) {
            dataTracker.set(OWNER_UUID, Optional.of(nbt.getUuid("Owner")));
        }
        else {
            dataTracker.set(OWNER_UUID, Optional.empty());
        }

        inventory.clear();
        if (nbt.contains("Inventory")) {
            NbtList list = (NbtList)nbt.get("Inventory");
            for (int i = 0; i < list.size(); i++) {
                inventory.add(ItemStack.fromNbt(list.getCompound(i)));
            }
        }
    }

    private void showEmoteParticle(boolean positive) {
        ParticleEffect particleEffect = ParticleTypes.HEART;
        if (!positive) {
            particleEffect = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d = random.nextGaussian() * 0.02D;
            double e = random.nextGaussian() * 0.02D;
            double f = random.nextGaussian() * 0.02D;
            world.addParticle(particleEffect, getParticleX(1.0D), getRandomBodyY() + 0.5D, getParticleZ(1.0D), d, e, f);
        }
    }

    private void depositItems() {
        ItemEntity itemEntity = new ItemEntity(world, getX(), getY() + 0.5D, getZ(), getMainHandStack());
        itemEntity.setPickupDelay(40);
        world.spawnEntity(itemEntity);
        equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        for (ItemStack stack : inventory) {
            ItemEntity item = new ItemEntity(world, getX(), getY() + 0.5D, getZ(), stack);
            item.setPickupDelay(40);
            world.spawnEntity(item);
        }
        inventory.clear();
    }

    public void talkToOwner(String message) {
        PlayerEntity player = getOwner();
        if (player != null) {
            player.sendSystemMessage(new LiteralText("<Allay> " + message), getUuid());
        }
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    public static DefaultAttributeContainer.Builder createAllayAttributes() {
        return DefaultAttributeContainer.builder()
                .add(EntityAttributes.GENERIC_MAX_HEALTH)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 1.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D);
    }

    private static class PickupItemsGoal extends Goal {
        private final Allay allay;
        private final double range;

        private ItemEntity destItem = null;

        public PickupItemsGoal(Allay allay, double range) {
            this.allay = allay;
            this.range = range;
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (allay.getNavigation().isFollowingPath()) {
                return false;
            }
            return !allay.world.getEntitiesByClass(ItemEntity.class, allay.getBoundingBox().expand(range), (target) -> target.getStack().getItem() == allay.getMainHandStack().getItem()).isEmpty();
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            List<ItemEntity> itemEntityList = allay.world.getEntitiesByClass(ItemEntity.class, allay.getBoundingBox().expand(range), (target) -> target.getStack().getItem() == allay.getMainHandStack().getItem());
            Optional<ItemEntity> item = itemEntityList.stream().min(Comparator.comparingDouble(allay::distanceTo));

            if (item.isPresent()) {
                destItem = item.get();
                allay.getNavigation().startMovingTo(destItem, 1.0D);
            }
            else {
                destItem = null;
            }
        }

        @Override
        public void tick() {
            if (destItem != null && !destItem.isAlive()) {
                destItem = null;
            }

            if (destItem != null) {
                if (!allay.getNavigation().isFollowingPath()) {
                    ItemStack stack = destItem.getStack();
                    allay.inventory.add(stack.copy());
                    allay.sendPickup(destItem, stack.getCount());
                    destItem.kill();

                    destItem = null;
                }
            }
        }
    }

    private static class FollowOwnerGoal extends Goal {
        private final Allay allay;

        public FollowOwnerGoal(Allay allay) {
            this.allay = allay;
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return !allay.getNavigation().isFollowingPath() && allay.getOwner() != null && allay.getOwner().distanceTo(allay) > 2.0D;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            super.start();
            allay.getNavigation().startMovingTo(allay.getOwner(), 1.0D);
        }
    }

    private static class FlyRandomlyGoal extends Goal {
        private final Allay allay;
        private final double range;

        public FlyRandomlyGoal(Allay allay, double range) {
            this.allay = allay;
            this.range = range;
            setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return !allay.getNavigation().isFollowingPath();
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            super.start();
            Random random = allay.getRandom();
            allay.getNavigation().startMovingTo(
                    allay.getX() + zeroOneToNegativeOneOne(random.nextDouble()) * range,
                    allay.getY() + zeroOneToNegativeOneOne(random.nextDouble()) * range,
                    allay.getZ() + zeroOneToNegativeOneOne(random.nextDouble()) * range,
                    1.0D);
        }

        private double zeroOneToNegativeOneOne(double x) {
            return x * 2.0D - 1.0D;
        }
    }
}
