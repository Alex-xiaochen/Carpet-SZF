package com.szf.mixin;

import com.szf.SZFSettings;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public class customFireworkCooldownMixin {
    @Inject(method = "useOn", at = @At("TAIL"))
    private void carpetSZF$applyCooldown(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (SZFSettings.customFireworkCooldown <= 0) return;
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        if (level.isClientSide()) return;
        if (player == null || player.isFallFlying()) return;
        if (itemStack.isEmpty()) return;

        player.getCooldowns().addCooldown(itemStack, SZFSettings.customFireworkCooldown);
    }

    @Inject(method = "use", at = @At("TAIL"))
    private void carpetSZF$applyCooldownElytra(Level level, Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (SZFSettings.customFireworkCooldown <= 0) return;
        if (level.isClientSide()) return;
        if (!player.isFallFlying()) return;
        if (cir.getReturnValue() != InteractionResult.SUCCESS) return;

        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.isEmpty()) return;

        player.getCooldowns().addCooldown(itemStack, SZFSettings.customFireworkCooldown);
    }
}
