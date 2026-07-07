package com.szf.mixin;
import com.szf.SZFSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
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
            if (source.getEntity() instanceof Creeper creeper && creeper.isPowered()) {
                entity.spawnAtLocation(level, new ItemStack(Items.PLAYER_HEAD, 1));
            }
        }

    }
}
