package com.szf.mixin;

import com.szf.SZFSettings;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class InventoryTotemMixin {
    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void carpetSZF$checkInventoryTotem(DamageSource killingDamage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self instanceof ServerPlayer serverPlayer) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack itemStack = serverPlayer.getItemInHand(hand);
                DeathProtection protection = itemStack.get(DataComponents.DEATH_PROTECTION);
                if (protection != null) {
                    ItemStack protectionItem = itemStack.copy();
                    itemStack.shrink(1);

                    serverPlayer.awardStat(Stats.ITEM_USED.get(protectionItem.getItem()));
                    CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, protectionItem);
                    protectionItem.causeUseVibration(self, GameEvent.ITEM_INTERACT_FINISH);

                    self.setHealth(1.0F);
                    protection.applyEffects(protectionItem, self);
                    self.level().broadcastEntityEvent(self, (byte)35);
                    cir.setReturnValue(true);
                    return;
                }
            }
        } else {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack itemStack = self.getItemInHand(hand);
                DeathProtection protection = itemStack.get(DataComponents.DEATH_PROTECTION);
                if (protection != null) {
                    ItemStack protectionItem = itemStack.copy();
                    itemStack.shrink(1);

                    self.setHealth(1.0F);
                    protection.applyEffects(protectionItem, self);
                    self.level().broadcastEntityEvent(self, (byte)35);
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        if (!SZFSettings.totemInInventory) {
            cir.setReturnValue(false);
            return;
        }

        if (!(self instanceof Player player)) {
            cir.setReturnValue(false);
            return;
        }
        if (!(self instanceof ServerPlayer sp)) {
            cir.setReturnValue(false);
            return;
        }

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            DeathProtection protection = stack.get(DataComponents.DEATH_PROTECTION);
            if (protection != null) {
                ItemStack protectionItem = stack.copy();
                stack.shrink(1);

                sp.awardStat(Stats.ITEM_USED.get(protectionItem.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger(sp, protectionItem);
                protectionItem.causeUseVibration(self, GameEvent.ITEM_INTERACT_FINISH);

                self.setHealth(1.0F);
                protection.applyEffects(protectionItem, self);
                self.level().broadcastEntityEvent(self, (byte)35);
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }
}
