package com.szf.mixin;

import com.szf.SZFSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlock.class)
public class DecoratedPotExtractMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void carpetSZF$extractFromEmptyHand(
        net.minecraft.world.level.block.state.BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!SZFSettings.decoratedPotCanExtract) return;
        if (!player.getMainHandItem().isEmpty()) return;
        if (level.isClientSide()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecoratedPotBlockEntity decoratedPot)) return;

        ItemStack stored = decoratedPot.getTheItem();
        if (stored.isEmpty()) return;

        ItemStack toGive = stored.copy();
        if (!player.getInventory().add(toGive)) return;

        decoratedPot.setTheItem(ItemStack.EMPTY);
        decoratedPot.setChanged();
        level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
