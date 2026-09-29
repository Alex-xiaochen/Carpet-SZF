package com.szf.mixin;
import com.szf.SZFSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Creeper.class)
public class CreeperMixin {
    @Inject(method = "killedEntity", at = @At("HEAD"))
    public void dieMixin1(ServerLevel level, LivingEntity entity, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if(SZFSettings.playerDropsHead){
            // Only players drop a head; without this any mob killed by a charged
            // creeper would spawn a PLAYER_HEAD.
            if (!(entity instanceof Player)) {
                return;
            }
            if (source.getEntity() instanceof Creeper creeper && creeper.isPowered()) {
                entity.spawnAtLocation(level, new ItemStack(Items.PLAYER_HEAD, 1));
            }
        }

    }
}
