package com.szf.mixin;

import com.szf.SZFSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class EndPortalFrameBlockMixin {
    @Inject(method = "getDestroyProgress(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F", at = @At("HEAD"), cancellable = true)
    private void carpetSZF$customFrameHardness(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        float hardness = SZFSettings.endPortalFrameHardness;
        if (hardness > 0 && state.getBlock() instanceof EndPortalFrameBlock) {
            cir.setReturnValue(1.0F / Math.max(hardness, 0.01F));
        }
    }
}
