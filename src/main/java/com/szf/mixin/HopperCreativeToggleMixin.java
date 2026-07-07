package com.szf.mixin;

import com.szf.SZFSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlock.class)
public class HopperCreativeToggleMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void carpetSZF$toggleHopper(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!SZFSettings.hopperCreativeToggle) return;
        if (!player.isCreative()) return;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean holdingFlintAndSteel = mainHand.is(Items.FLINT_AND_STEEL) || offHand.is(Items.FLINT_AND_STEEL);
        if (!holdingFlintAndSteel) return;

        if (!level.isClientSide()) {
            boolean current = state.getValue(HopperBlock.ENABLED);
            boolean next = !current;
            level.setBlock(pos, state.setValue(HopperBlock.ENABLED, next), 2);

            level.playSound(null, pos, next ? SoundEvents.DISPENSER_FAIL : SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
