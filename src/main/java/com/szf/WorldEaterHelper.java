package com.szf;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class WorldEaterHelper {
    private static final Map<UUID, TaggedCart> TAGGED = new HashMap<>();
    private static final double LOOK_RANGE = 6.0;
    private static final double CONTINUOUS_HORIZONTAL_THRESHOLD = 3.0;
    private static final double MOVEMENT_EPSILON_SQR = 1.0E-6;

    private WorldEaterHelper() {
    }

    private static final class TaggedCart {
        private final UUID owner;
        private final ResourceKey<Level> dimension;
        private double lastX;
        private double lastY;
        private double lastZ;
        private double continuousHorizontalDistance;
        private int stuckTicks;
        private boolean alerted;

        private TaggedCart(UUID owner, ResourceKey<Level> dimension, Vec3 position) {
            this.owner = owner;
            this.dimension = dimension;
            this.lastX = position.x();
            this.lastY = position.y();
            this.lastZ = position.z();
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(build("WorldEaterHelper"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build(String name) {
        return Commands.literal(name)
            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("Tag").executes(WorldEaterHelper::tag))
            .then(Commands.literal("Clear").executes(WorldEaterHelper::clear))
            .then(Commands.literal("SetTime")
                .then(Commands.argument("seconds", IntegerArgumentType.integer(1))
                    .executes(WorldEaterHelper::setTime)));
    }

    private static int tag(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("此指令只能由玩家执行"));
            return 0;
        }

        Entity target = findLookedAtEntity(player);
        if (!(target instanceof AbstractMinecart)) {
            source.sendFailure(Component.literal("你必须看着一个矿车"));
            return 0;
        }
        if (!(target instanceof Minecart cart)) {
            source.sendFailure(Component.literal("此指令只对普通矿车有效"));
            return 0;
        }

        Vec3 position = cart.position();
        TAGGED.put(cart.getUUID(), new TaggedCart(player.getUUID(), cart.level().dimension(), position));
        source.sendSuccess(() -> Component.literal("已标记矿车 " + formatPosition(position)), false);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("此指令只能由玩家执行"));
            return 0;
        }

        Entity target = findLookedAtEntity(player);
        if (target instanceof Minecart cart) {
            boolean removed = TAGGED.remove(cart.getUUID()) != null;
            source.sendSuccess(() -> Component.literal(removed ? "已取消此矿车的标记" : "此矿车没有被标记"), false);
            return removed ? 1 : 0;
        }

        int count = TAGGED.size();
        TAGGED.clear();
        source.sendSuccess(() -> Component.literal("已清除所有被标记的矿车，共 " + count + " 个"), false);
        return count;
    }

    private static int setTime(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        SZFSettings.worldEaterHelperStuckSeconds = seconds;
        source.sendSuccess(() -> Component.literal("已将卡住判定时间设置为 " + seconds + " 秒"), false);
        return seconds;
    }

    public static void onServerTick(MinecraftServer server) {
        if (TAGGED.isEmpty()) {
            return;
        }

        int thresholdTicks = Math.max(1, SZFSettings.worldEaterHelperStuckSeconds) * 20;

        Iterator<Map.Entry<UUID, TaggedCart>> iterator = TAGGED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, TaggedCart> entry = iterator.next();
            TaggedCart cart = entry.getValue();

            ServerLevel level = server.getLevel(cart.dimension);
            Entity entity = level == null ? null : level.getEntity(entry.getKey());
            if (!(entity instanceof Minecart minecart) || minecart.isRemoved()) {
                iterator.remove();
                continue;
            }

            Vec3 position = minecart.position();
            double movedX = position.x() - cart.lastX;
            double movedY = position.y() - cart.lastY;
            double movedZ = position.z() - cart.lastZ;
            double movedSqr = movedX * movedX + movedY * movedY + movedZ * movedZ;
            if (movedSqr > MOVEMENT_EPSILON_SQR) {
                cart.continuousHorizontalDistance += Math.sqrt(movedX * movedX + movedZ * movedZ);
                cart.lastX = position.x();
                cart.lastY = position.y();
                cart.lastZ = position.z();
                cart.stuckTicks = 0;
                cart.alerted = false;
                if (cart.continuousHorizontalDistance >= CONTINUOUS_HORIZONTAL_THRESHOLD) {
                    iterator.remove();
                    continue;
                }
            } else {
                cart.continuousHorizontalDistance = 0.0;
                cart.stuckTicks++;
                if (!cart.alerted && cart.stuckTicks >= thresholdTicks) {
                    alert(server, cart, position);
                    cart.alerted = true;
                }
            }
        }
    }

    private static void alert(MinecraftServer server, TaggedCart cart, Vec3 position) {
        ServerPlayer owner = server.getPlayerList().getPlayer(cart.owner);
        if (owner == null) {
            return;
        }

        Component title = Component.literal(dimensionName(cart.dimension) + "世吞已卡住");
        Component subtitle = Component.literal(formatPosition(position));
        owner.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
        owner.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        owner.connection.send(new ClientboundSetTitleTextPacket(title));
    }

    private static Entity findLookedAtEntity(ServerPlayer player) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(LOOK_RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(LOOK_RANGE)).inflate(1.0);

        Entity best = null;
        double bestSqr = LOOK_RANGE * LOOK_RANGE;
        for (Entity entity : player.level().getEntities(player, searchBox, e -> e instanceof AbstractMinecart && e.isPickable())) {
            AABB box = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = box.clip(start, end);
            double sqr;
            if (hit.isPresent()) {
                sqr = start.distanceToSqr(hit.get());
            } else if (box.contains(start)) {
                sqr = 0.0;
            } else {
                continue;
            }
            if (sqr < bestSqr) {
                bestSqr = sqr;
                best = entity;
            }
        }
        return best;
    }

    private static String dimensionName(ResourceKey<Level> dimension) {
        Identifier id = dimension.identifier();
        return switch (id.getPath()) {
            case "overworld" -> "主世界";
            case "the_nether" -> "下界";
            case "the_end" -> "末地";
            default -> id.getPath();
        };
    }

    private static String formatPosition(Vec3 position) {
        return String.format("(%.1f, %.1f, %.1f)", position.x(), position.y(), position.z());
    }
}
