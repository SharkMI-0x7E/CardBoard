package org.cardboardpowered.mixin.server.network;

import java.util.Collections;
import java.util.Set;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spigotmc.SpigotConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.network.ServerGamePacketListenerImplBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;

import io.papermc.paper.event.player.PlayerFailMoveEvent;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 801)
public class ServerGamePacketListenerImplMixin_PlayerMove {

	// Cardboard - start
	private boolean hasMoved = false;
	private int lastTick = 0;
	public int allowedPlayerTicks = 1;
	private double lastPosX = Double.MAX_VALUE;
	private double lastPosY = Double.MAX_VALUE;
	private double lastPosZ = Double.MAX_VALUE;
	private float lastPitch = Float.MAX_VALUE;
	private float lastYaw = Float.MAX_VALUE;
	private boolean justTeleported = false;
	// Cardboard - end
	
	@Shadow public double firstGoodX;
    @Shadow public double firstGoodY;
    @Shadow public double firstGoodZ;
    @Shadow public double lastGoodX;
    @Shadow public double lastGoodY;
    @Shadow public double lastGoodZ;
    @Shadow private boolean clientIsFloating;
    @Shadow private int receivedMovePacketCount;
    @Shadow private int knownMovePacketCount;
	
	@Shadow public ServerPlayer player;
	@Shadow public void resetPosition() {}
	@Shadow private void handlePlayerKnownMovement(Vec3 movement) {}
	@Shadow private boolean updateAwaitingTeleport() {return false;}
	@Shadow private boolean noBlocksAround(net.minecraft.world.entity.Entity entity) { return false; }
	@Shadow private boolean shouldCheckPlayerMovement(boolean elytra) {return false;}
	// @Shadow private boolean hasNewCollision(ServerWorld world, net.minecraft.entity.Entity entity, Box oldBox, Box newBox) {return false;}
	// @Shadow private void internalTeleport(double d0, double d1, double d2, float f, float f1) {};
	@Shadow private static boolean containsInvalidValues(double x, double y, double z, float yaw, float pitch) {return false;}
	
	private boolean hasNewCollision(ServerLevel world, net.minecraft.world.entity.Entity entity, AABB oldBox, AABB newBox) {
        /*int i;
        ArrayList<Box> collisionsBB = new ArrayList<Box>();
        ArrayList<VoxelShape> collisionsVoxel = new ArrayList<VoxelShape>();
        CollisionUtil.getCollisions(world, entity, newBox, collisionsVoxel, collisionsBB, 6, null, null);
        int len = collisionsBB.size();
        for (i = 0; i < len; ++i) {
            Box box = (Box)collisionsBB.get(i);
            if (CollisionUtil.voxelShapeIntersect(box, oldBox)) continue;
            return true;
        }
        len = collisionsVoxel.size();
        for (i = 0; i < len; ++i) {
            VoxelShape voxel = (VoxelShape)collisionsVoxel.get(i);
            if (CollisionUtil.voxelShapeIntersectNoEmpty(voxel, oldBox)) continue;
            return true;
        }*/
        return false;
    }
	
	@Shadow
	public void teleport( PositionMoveRotation pos, Set<Relative> flags) {}
	
	@Shadow
    public void teleport(double d0, double d1, double d2, float f, float f1) {}
	
	@Shadow
    public int tickCount;
	
	@Shadow
    public Vec3 awaitingPositionFromClient;
	
	public CraftPlayer getCraftPlayer() {
        return this.player == null ? null : (CraftPlayer) ((EntityBridge)this.player).getBukkitEntity();
    }
	
	// Cardboard - Paper start
	private PlayerFailMoveEvent fireFailMove(PlayerFailMoveEvent.FailReason failReason, double toX, double toY, double toZ, float toYaw, float toPitch, boolean logWarning) {
        CraftPlayer player = this.getCraftPlayer();
        Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
        Location to = new Location(player.getWorld(), toX, toY, toZ, toYaw, toPitch);
        PlayerFailMoveEvent event = new PlayerFailMoveEvent(player, failReason, false, logWarning, from, to);
        event.callEvent();
        return event;
    }
	// Cardboard - Paper end
	
	/**
	 * @author Cardboard Mod
	 * @reason Bukkit Teleport
	 */
	@Overwrite
	public void handleMovePlayer(ServerboundMovePlayerPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl)(Object)this, this.player.level());
        if (containsInvalidValues(packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYRot(0.0f), packet.getXRot(0.0f))) {
            // this.disconnect(Text.translatable("multiplayer.disconnect.invalid_player_movement"), PlayerKickEvent.Cause.INVALID_PLAYER_MOVEMENT);
        } else {
            ServerLevel worldserver = this.player.level();
            if (!this.player.wonGame /*&& !this.player.isImmobile()*/) {
                if (this.tickCount == 0) {
                    this.resetPosition();
                }

                // Cardboard: Mojmap: hasClientLoaded
                boolean this_player_isLoaded = ((ServerGamePacketListenerImpl)(Object)this).hasClientLoaded();

                if (!this.updateAwaitingTeleport() && this_player_isLoaded) {
                    float f1;
                    float f;
                    double d2;
                    double d1;
                    double d0;
                    double toX = d0 = ServerGamePacketListenerImpl.clampHorizontal(packet.getX(this.player.getX()));
                    double toY = d1 = ServerGamePacketListenerImpl.clampVertical(packet.getY(this.player.getY()));
                    double toZ = d2 = ServerGamePacketListenerImpl.clampHorizontal(packet.getZ(this.player.getZ()));
                    float toYaw = f = Mth.wrapDegrees(packet.getYRot(this.player.getYRot()));
                    float toPitch = f1 = Mth.wrapDegrees(packet.getXRot(this.player.getXRot()));
                    if (this.player.isPassenger()) {
                        this.player.absSnapTo(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                        this.player.level().getChunkSource().move(this.player);
                        this.allowedPlayerTicks = 20;
                    } else {
                        double prevX = this.player.getX();
                        double prevY = this.player.getY();
                        double prevZ = this.player.getZ();
                        float prevYaw = this.player.getYRot();
                        float prevPitch = this.player.getXRot();
                        double d3 = this.player.getX();
                        double d4 = this.player.getY();
                        double d5 = this.player.getZ();
                        double d6 = d0 - this.firstGoodX;
                        double d7 = d1 - this.firstGoodY;
                        double d8 = d2 - this.firstGoodZ;
                        double d9 = this.player.getDeltaMovement().lengthSqr();
                        double currDeltaX = toX - prevX;
                        double currDeltaY = toY - prevY;
                        double currDeltaZ = toZ - prevZ;
                        double d10 = Math.max(d6 * d6 + d7 * d7 + d8 * d8, currDeltaX * currDeltaX + currDeltaY * currDeltaY + currDeltaZ * currDeltaZ - 1.0);
                        double otherFieldX = d0 - this.lastGoodX;
                        double otherFieldY = d1 - this.lastGoodY;
                        double otherFieldZ = d2 - this.lastGoodZ;
                        d10 = Math.max(d10, otherFieldX * otherFieldX + otherFieldY * otherFieldY + otherFieldZ * otherFieldZ - 1.0);
                        if (this.player.isSleeping()) {
                            if (d10 > 1.0) {
                                this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, f1);
                            }
                        } else {
                            PlayerFailMoveEvent event;
                            PlayerFailMoveEvent event2;
                            boolean didCollide;
                            boolean flag1;
                            boolean flag = this.player.isFallFlying();
                            if (worldserver.tickRateManager().runsNormally()) {
                                PlayerFailMoveEvent event3;
                                ++this.receivedMovePacketCount;
                                int i = this.receivedMovePacketCount - this.knownMovePacketCount;
                                this.allowedPlayerTicks = (int)((long)this.allowedPlayerTicks + (System.currentTimeMillis() / 50L - (long)this.lastTick));
                                this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                                this.lastTick = (int)(System.currentTimeMillis() / 50L);
                                if (i > Math.max(this.allowedPlayerTicks, 5)) {
                                    // LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", (Object)this.player.getName().getString(), (Object)i);
                                    i = 1;
                                }
                                this.allowedPlayerTicks = packet.hasRot || d10 > 0.0 ? --this.allowedPlayerTicks : 20;
                                double speed = this.player.getAbilities().flying ? (double)(this.player.getAbilities().flyingSpeed * 20.0f) : (double)(this.player.getAbilities().walkingSpeed * 10.0f);
                                /*
                                if (
                                		!(
                                				/*!this.player.getWorld().paperConfig().chunks.preventMovingIntoUnloadedChunks
                                				|| this.player.getX() == toX && this.player.getZ() == toZ ||
                                				worldserver.areChunksLoadedForMove(this.player.getBoundingBox().stretch(new Vec3d(toX, toY, toZ).subtract(this.player.getPos()))) || (event3 = this.fireFailMove(PlayerFailMoveEvent.FailReason.MOVED_INTO_UNLOADED_CHUNK, toX, toY, toZ, toYaw, toPitch, false)).isAllowed())) {
                                    this.internalTeleport(PlayerPosition.fromEntity(this.player), Collections.emptySet());
                                    return;
                                }*/
                                if (this.shouldCheckPlayerMovement(flag)) {
                                    PlayerFailMoveEvent event4;
                                    float f2;
                                    float f3 = f2 = flag ? 300.0f : 100.0f;
                                    if (d10 - d9 > Math.max((double)f2, Math.pow(SpigotConfig.movedTooQuicklyMultiplier * (double)i * speed, 2.0)) && !(event4 = this.fireFailMove(PlayerFailMoveEvent.FailReason.MOVED_TOO_QUICKLY, toX, toY, toZ, toYaw, toPitch, true)).isAllowed()) {
                                        if (event4.getLogWarning()) {
                                            // LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.player.getName().getString(), d6, d7, d8});
                                        }
                                        this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                        return;
                                    }
                                }
                            }
                            AABB axisalignedbb = this.player.getBoundingBox();
                            d6 = d0 - this.lastGoodX;
                            d7 = d1 - this.lastGoodY;
                            d8 = d2 - this.lastGoodZ;
                            boolean bl = flag1 = d7 > 0.0;
                            if (this.player.onGround() && !packet.isOnGround() && flag1) {
                                PlayerJumpEvent event5;
                                CraftPlayer player = this.getCraftPlayer();
                                Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
                                Location to = player.getLocation().clone();
                                if (packet.hasPosition()) {
                                    to.setX(packet.x);
                                    to.setY(packet.y);
                                    to.setZ(packet.z);
                                }
                                if (packet.hasRotation()) {
                                    to.setYaw(packet.yRot);
                                    to.setPitch(packet.xRot);
                                }
                                if ((event5 = new PlayerJumpEvent((Player)player, from, to)).callEvent()) {
                                    this.player.jumpFromGround();
                                } else {
                                    from = event5.getFrom();
                                    ((ServerGamePacketListenerImplBridge)(Object)this).cardboard$internalTeleport(new PositionMoveRotation(CraftLocation.toVec3(from), Vec3.ZERO, from.getYaw(), from.getPitch()), Collections.emptySet());
                                    return;
                                }
                            }
                            boolean flag2 = this.player.verticalCollisionBelow;
                            this.player.move(MoverType.PLAYER, new Vec3(d6, d7, d8));
                            this.player.onGround = packet.isOnGround();
                            boolean bl2 = didCollide = toX != this.player.getX() || toY != this.player.getY() || toZ != this.player.getZ();
                            if (this.awaitingPositionFromClient != null) {
                                return;
                            }
                            double d11 = d7;
                            d6 = d0 - this.player.getX();
                            d7 = d1 - this.player.getY();
                            if (d7 > -0.5 || d7 < 0.5) {
                                d7 = 0.0;
                            }
                            d8 = d2 - this.player.getZ();
                            d10 = d6 * d6 + d7 * d7 + d8 * d8;
                            boolean movedWrongly = false;
                            if (!(this.player.isChangingDimension() || !(d10 > SpigotConfig.movedWronglyThreshold) || this.player.isSleeping() || this.player.gameMode.isCreative() || this.player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || (event2 = this.fireFailMove(PlayerFailMoveEvent.FailReason.MOVED_WRONGLY, toX, toY, toZ, toYaw, toPitch, true)).isAllowed())) {
                                movedWrongly = true;
                                if (event2.getLogWarning()) {
                                    // LOGGER.warn("{} moved wrongly!", (Object)this.player.getName().getString());
                                }
                            }
                            boolean teleportBack = !this.player.noPhysics && !this.player.isSleeping() && movedWrongly;
                            this.player.absSnapTo(d0, d1, d2, f, f1);
                            if (!(this.player.noPhysics || this.player.isSleeping() || teleportBack)) {
                                AABB newBox = this.player.getBoundingBox();
                                if (didCollide || !axisalignedbb.equals(newBox)) {
                                    teleportBack = this.hasNewCollision(worldserver, this.player, axisalignedbb, newBox);
                                }
                            }
                            if (teleportBack && (event = this.fireFailMove(PlayerFailMoveEvent.FailReason.CLIPPED_INTO_BLOCK, toX, toY, toZ, toYaw, toPitch, false)).isAllowed()) {
                                teleportBack = false;
                            }
                            if (teleportBack) {


                            	ServerGamePacketListenerImpl thiz = (ServerGamePacketListenerImpl)(Object)this;

                            	// thiz.teleport

                            	thiz.teleport(d3, d4, d5, f, f1);



                                this.player.doCheckFallDamage(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5, packet.isOnGround());
                            } else {
                                this.player.absSnapTo(prevX, prevY, prevZ, prevYaw, prevPitch);
                                CraftPlayer player = this.getCraftPlayer();
                                if (!this.hasMoved) {
                                    this.lastPosX = prevX;
                                    this.lastPosY = prevY;
                                    this.lastPosZ = prevZ;
                                    this.lastYaw = prevYaw;
                                    this.lastPitch = prevPitch;
                                    this.hasMoved = true;
                                }
                                Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
                                Location to = player.getLocation().clone();
                                if (packet.hasPos) {
                                    to.setX(packet.x);
                                    to.setY(packet.y);
                                    to.setZ(packet.z);
                                }
                                if (packet.hasRot) {
                                    to.setYaw(packet.yRot);
                                    to.setPitch(packet.xRot);
                                }
                                double delta = Math.pow(this.lastPosX - to.getX(), 2.0) + Math.pow(this.lastPosY - to.getY(), 2.0) + Math.pow(this.lastPosZ - to.getZ(), 2.0);
                                float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());
                                if ((delta > 0.00390625 || deltaAngle > 10.0f) /*&& !this.player.isImmobile()*/) {
                                    this.lastPosX = to.getX();
                                    this.lastPosY = to.getY();
                                    this.lastPosZ = to.getZ();
                                    this.lastYaw = to.getYaw();
                                    this.lastPitch = to.getPitch();
                                    Location oldTo = to.clone();
                                    PlayerMoveEvent event6 = new PlayerMoveEvent(player, from, to);
                                    CraftServer.INSTANCE.getPluginManager().callEvent(event6);
                                    if (event6.isCancelled()) {
                                        ((ServerGamePacketListenerImplBridge)(Object)this).teleport(from);
                                        return;
                                    }
                                    if (!oldTo.equals((Object)event6.getTo()) && !event6.isCancelled()) {
                                        ((ServerPlayerBridge)this.player).getBukkitEntity().teleport(event6.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                        return;
                                    }
                                    if (!from.equals((Object)this.getCraftPlayer().getLocation()) && this.justTeleported) {
                                        this.justTeleported = false;
                                        return;
                                    }
                                }
                                this.player.absSnapTo(d0, d1, d2, f, f1);
                                boolean flag4 = this.player.isAutoSpinAttack();
                                this.clientIsFloating = d11 >= -0.03125 && !flag2 &&
                                		this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR &&
                                		!CraftServer.server.allowFlight() &&
                                		!this.player.getAbilities().mayfly &&
                                		!this.player.hasEffect(MobEffects.LEVITATION) &&
                                		!flag && !flag4 && this.noBlocksAround(this.player);
                                this.player.level().getChunkSource().move(this.player);
                                Vec3 vec3d = new Vec3(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.player.setOnGroundWithMovement(packet.isOnGround(), packet.horizontalCollision(), vec3d);
                                this.player.doCheckFallDamage(vec3d.x, vec3d.y, vec3d.z, packet.isOnGround());

                                // TODO: 1.21.8: Seems this is gone?
                                // this.player.queueBlockCollisionCheck(new Vec3d(d3, d4, d5), this.player.getPos());

                                this.handlePlayerKnownMovement(vec3d);
                                if (flag1) {
                                    this.player.resetFallDistance();
                                }
                                if (packet.isOnGround() || this.player.hasLandedInLiquid() || this.player.onClimbable() || this.player.isSpectator() || flag || flag4) {
                                    this.player.tryResetCurrentImpulseContext();
                                }
                                this.player.checkMovementStatistics(this.player.getX() - d3, this.player.getY() - d4, this.player.getZ() - d5);
                                this.lastGoodX = this.player.getX();
                                this.lastGoodY = this.player.getY();
                                this.lastGoodZ = this.player.getZ();
                            }
                        }
                    }
                }
            }
        }
    }
	
}
