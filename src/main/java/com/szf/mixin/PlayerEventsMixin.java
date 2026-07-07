package com.szf.mixin;

import com.mojang.authlib.GameProfile;
import com.szf.SZFSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerEventsMixin extends Player {
    public PlayerEventsMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void carpetSZF$blockDuZhaoYang(MinecraftServer server, net.minecraft.server.level.ServerLevel level, GameProfile profile, ClientInformation clientInfo, CallbackInfo ci) {
        if (SZFSettings.blockPlayerDuZhaoYang && "DuZhaoYang".equalsIgnoreCase(profile.name())) {
            ServerPlayer self = (ServerPlayer)(Object)this;
            ServerGamePacketListenerImpl conn = self.connection;
            if (conn != null) {
                conn.disconnect(Component.literal("你已经被禁止加入此服务器。"));
            }
        }
    }
}
