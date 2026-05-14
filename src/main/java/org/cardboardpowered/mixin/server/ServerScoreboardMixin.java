package org.cardboardpowered.mixin.server;

import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;
import org.cardboardpowered.bridge.network.protocol.game.ClientboundSetPlayerTeamPacketBridge;
import org.cardboardpowered.bridge.server.ServerScoreboardBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.Set;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

@Mixin(value = ServerScoreboard.class, priority = 900)
public abstract class ServerScoreboardMixin extends Scoreboard implements ServerScoreboardBridge {

    @Shadow
    public Set<Objective> trackedObjectives;

    /*
    public void addScoreboardObjective(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = ((ServerScoreboard)(Object)this).createChangePackets(scoreboardobjective);
        Iterator iterator = CraftServer.INSTANCE.getHandle().getPlayerManager().getPlayerList().iterator();

        while (iterator.hasNext()) {
            ServerPlayerEntity entityplayer = (ServerPlayerEntity) iterator.next();
            if (((CraftPlayer)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).getScoreboard().getHandle() != (ServerScoreboard)(Object)this) continue; // Bukkit - Only players on this board
            Iterator iterator1 = list.iterator();

            while (iterator1.hasNext()) {
                Packet<?> packet = (Packet) iterator1.next();
                entityplayer.networkHandler.sendPacket(packet);
            }
        }

        this.syncableObjectives.add(scoreboardobjective);
    }

    public void removeScoreboardObjective(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = ((ServerScoreboard)(Object)this).createRemovePackets(scoreboardobjective);
        Iterator iterator = CraftServer.INSTANCE.getHandle().getPlayerManager().getPlayerList().iterator();

        while (iterator.hasNext()) {
            ServerPlayerEntity entityplayer = (ServerPlayerEntity) iterator.next();
            if (((CraftPlayer)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).getScoreboard().getHandle() != (ServerScoreboard)(Object)this) continue; // Bukkit - Only players on this board
            Iterator iterator1 = list.iterator();

            while (iterator1.hasNext()) {
                Packet<?> packet = (Packet) iterator1.next();
                entityplayer.networkHandler.sendPacket(packet);
            }
        }

        this.syncableObjectives.remove(scoreboardobjective);
    }

    private void sendAll(Packet packet) {
        for (ServerPlayerEntity entityplayer : CraftServer.server.getPlayerManager().players)
            if (((CraftPlayer)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).getScoreboard().getHandle() == (ServerScoreboard)(Object)this)
                entityplayer.networkHandler.sendPacket(packet);
    }
    */

    @Shadow
    protected abstract void setDirty();

    @Shadow
    @Final
    private MinecraftServer server;

    /**
     * @author Cardboard
     * @reason bukkitize scoreboard
     */
    @Overwrite
    public void startTrackingObjective(Objective objective) {
        List<Packet<?>> list = ((ServerScoreboard)(Object)this).getStartTrackingPackets(objective);
        for (ServerPlayer entityplayer : CraftServer.INSTANCE.getHandle().getPlayers()) {
            if (((CraftPlayer)((ServerPlayerBridge)entityplayer).getBukkitEntity()).getScoreboard().getHandle() != (ServerScoreboard)(Object)this) continue;
            for (Packet<?> packet : list) {
                entityplayer.connection.send(packet);
            }
        }
        this.trackedObjectives.add(objective);
    }
    
    /**
     * @author Cardboard
     * @reason bukkitize scoreboard
     */
    @Overwrite
    public void stopTrackingObjective(Objective objective) {
        List<Packet<?>> list = ((ServerScoreboard)(Object)this).getStopTrackingPackets(objective);
        for (ServerPlayer entityplayer : CraftServer.INSTANCE.getHandle().getPlayers()) {
            if (((CraftPlayer)((ServerPlayerBridge)entityplayer).getBukkitEntity()).getScoreboard().getHandle() != (ServerScoreboard)(Object)this) continue;
            for (Packet<?> packet : list) {
                entityplayer.connection.send(packet);
            }
        }
        this.trackedObjectives.remove(objective);
    }

    // Paper start - Multiple Entries with Scoreboards
    @Override
    public boolean cardboard$addPlayersToTeam(java.util.Collection<String> players, PlayerTeam team) {
        boolean anyAdded = false;
        for (String playerName : players) {
            if (super.addPlayerToTeam(playerName, team)) {
                anyAdded = true;
            }
        }

        if (anyAdded) {
            this.broadcastAll(ClientboundSetPlayerTeamPacketBridge.createMultiplePlayerPacket(team, players, ClientboundSetPlayerTeamPacket.Action.ADD));
            this.setDirty();
            return true;
        } else {
            return false;
        }
    }
    // Paper end - Multiple Entries with Scoreboards

    // Paper start - Multiple Entries with Scoreboards
    @Override
    public void cardboard$removePlayersFromTeam(java.util.Collection<String> players, PlayerTeam team) {
        for (String playerName : players) {
            super.removePlayerFromTeam(playerName, team);
        }

        this.broadcastAll(ClientboundSetPlayerTeamPacketBridge.createMultiplePlayerPacket(team, players, ClientboundSetPlayerTeamPacket.Action.REMOVE));
        this.setDirty();
    }
    // Paper end - Multiple Entries with Scoreboards

    // CraftBukkit start - Send to players
    @Unique
    private void broadcastAll(Packet<?> packet) {
        for (ServerPlayer serverPlayer : this.server.getPlayerList().players) {
            if (((CraftPlayer)((EntityBridge)serverPlayer).getBukkitEntity()).getScoreboard().getHandle() == this) {
                serverPlayer.connection.send(packet);
            }
        }
    }
    // CraftBukkit end
}
