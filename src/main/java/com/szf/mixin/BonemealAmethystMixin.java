package com.szf.mixin;

import com.szf.SZFSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BonemealAmethystMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void carpetSZF$bonemealBuddingAmethyst(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!SZFSettings.bonemealAmethystBud) return;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof BuddingAmethystBlock)) return;

        Direction face = context.getClickedFace();
        BlockPos budPos = pos.relative(face);
        BlockState budState = level.getBlockState(budPos);

        Block nextBlock;
        boolean planting = false;
        if (isBudFacing(budState, face)) {
            nextBlock = nextStage(budState.getBlock());
            if (nextBlock == null) return;
        } else if (BuddingAmethystBlock.canClusterGrowAtState(budState)) {
            nextBlock = Blocks.SMALL_AMETHYST_BUD;
            planting = true;
        } else {
            return;
        }

        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        if (planting || level.getRandom().nextInt(10) == 0) {
            BlockState grown = nextBlock.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, face)
                    .setValue(AmethystClusterBlock.WATERLOGGED, budState.getFluidState().is(Fluids.WATER));
            level.setBlockAndUpdate(budPos, grown);
            level.playSound(null, budPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, budPos);
        }

        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        itemStack.shrink(1);
        if (player != null) {
            itemStack.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
        }
        level.levelEvent(1505, budPos, 15);
        cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
    }

    private static boolean isBudFacing(BlockState state, Direction face) {
        return state.getBlock() instanceof AmethystClusterBlock
                && state.getValue(AmethystClusterBlock.FACING) == face;
    }

    private static Block nextStage(Block block) {
        if (block == Blocks.SMALL_AMETHYST_BUD) return Blocks.MEDIUM_AMETHYST_BUD;
        if (block == Blocks.MEDIUM_AMETHYST_BUD) return Blocks.LARGE_AMETHYST_BUD;
        if (block == Blocks.LARGE_AMETHYST_BUD) return Blocks.AMETHYST_CLUSTER;
        return null;
    }
}
