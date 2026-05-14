package org.cardboardpowered.impl;

import java.util.Collection;
import java.util.List;
import net.minecraft.world.level.dimension.end.DragonRespawnAnimation;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import io.papermc.paper.math.Position;

public class CardboardDragonBattle implements DragonBattle {

    private final EndDragonFight handle;

    public CardboardDragonBattle(EndDragonFight handle) {
        this.handle = handle;
    }

    @Override
    public EnderDragon getEnderDragon() {
        return null; // TODO
    }

    @Override
    public BossBar getBossBar() {
        return null; // TODO
    }

    @Override
    public Location getEndPortalLocation() {
        return null; // TODO
    }

    @Override
    public boolean generateEndPortal(boolean withPortals) {
        // TODO
        return true;
    }

    @Override
    public boolean hasBeenPreviouslyKilled() {
        return handle.hasPreviouslyKilledDragon();
    }

    @Override
    public void initiateRespawn() {
        this.handle.tryRespawn();
    }

    @Override
    public RespawnPhase getRespawnPhase() {
        return RespawnPhase.NONE; // TODO
    }

    @Override
    public boolean setRespawnPhase(RespawnPhase phase) {
        // TODO
        return true;
    }

    @Override
    public void resetCrystals() {
        this.handle.resetSpikeCrystals();
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CardboardDragonBattle && ((CardboardDragonBattle) obj).handle == this.handle;
    }

    private RespawnPhase toBukkitRespawnPhase(DragonRespawnAnimation phase) {
        return (phase != null) ? RespawnPhase.values()[phase.ordinal()] : RespawnPhase.NONE;
    }

    private DragonRespawnAnimation toNMSRespawnPhase(RespawnPhase phase) {
        return (phase != RespawnPhase.NONE) ? DragonRespawnAnimation.values()[phase.ordinal()] : null;
    }

	@Override
	public boolean initiateRespawn(@Nullable Collection<EnderCrystal> enderCrystals) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getGatewayCount() {
		// TODO Auto-generated method stub
		// return 20 - this.handle.gateways.size();
		return -1;
	}

	@Override
	public boolean spawnNewGateway() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void spawnNewGateway(@NotNull Position position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public @NotNull @Unmodifiable List<EnderCrystal> getRespawnCrystals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull @Unmodifiable List<EnderCrystal> getHealingCrystals() {
		// TODO Auto-generated method stub
		return null;
	}

	// 1.20.4 API
	
	@Override
	public void setPreviouslyKilled(boolean previouslyKilled) {
        // TODO
		// this.handle.previouslyKilled = previouslyKilled;
	}
}
