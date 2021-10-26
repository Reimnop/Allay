package net.allay.main;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class PeeSeaItem extends Item {
    private static final Identifier PEE_SEA_SETBLOCK_ID = new Identifier("allay", "pee_sea_setblock");
    private static final Identifier PEE_SEA_FLOOD_FILL_ID = new Identifier("allay", "pee_sea_flood_fill");

    private static final KeyBinding FLOOD_FILL_KEYBIND = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.allay.flood_fill",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_LEFT_ALT,
                    "category.allay.keybinds"));

    public PeeSeaItem(Settings settings) {
        super(settings);

        ServerPlayNetworking.registerGlobalReceiver(PEE_SEA_SETBLOCK_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();

            server.execute(() -> {
                player.getServerWorld().setBlockState(pos, Blocks.YELLOW_WOOL.getDefaultState());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(PEE_SEA_FLOOD_FILL_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();

            server.execute(() -> {
                floodFill(player.getServerWorld(), pos, 0);
            });
        });
    }

    private void floodFill(World world, BlockPos pos, int depth) {
        if (depth > 12)
            return;

        Block block = world.getBlockState(pos).getBlock();

        if (block != Blocks.WATER)
            return;

        if (block != Blocks.YELLOW_WOOL)
            world.setBlockState(pos, Blocks.YELLOW_WOOL.getDefaultState());
        depth++;

        floodFill(world, pos.add(0, 0, 1), depth);
        floodFill(world, pos.add(0, 0, -1), depth);
        floodFill(world, pos.add(0, 1, 0), depth);
        floodFill(world, pos.add(0, -1, 0), depth);
        floodFill(world, pos.add(1, 0, 0), depth);
        floodFill(world, pos.add(-1, 0, 0), depth);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        HitResult hit = user.raycast(5.0, 1.0f, true);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult result = (BlockHitResult)hit;
            BlockState state = world.getBlockState(result.getBlockPos());
            Block block = state.getBlock();
            if (block == Blocks.WATER) {
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeBlockPos(result.getBlockPos());

                if (FLOOD_FILL_KEYBIND.isPressed()) {
                    ClientPlayNetworking.send(PEE_SEA_FLOOD_FILL_ID, buffer);
                }
                else {
                    ClientPlayNetworking.send(PEE_SEA_SETBLOCK_ID, buffer);
                }

                return TypedActionResult.success(user.getStackInHand(hand));
            }
        }

        return TypedActionResult.fail(user.getStackInHand(hand));
    }
}
