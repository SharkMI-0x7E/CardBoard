package org.cardboardpowered.impl;

import io.papermc.paper.InternalAPIBridge;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.item.PaperResolvableProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.world.damagesource.CombatEntry;
import io.papermc.paper.world.damagesource.FallLocationType;
import io.papermc.paper.world.damagesource.PaperCombatEntryWrapper;
import io.papermc.paper.world.damagesource.PaperCombatTrackerWrapper;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import net.kyori.adventure.text.Component;
import net.minecraft.Optionull;
import net.minecraft.world.damagesource.FallLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.bukkit.GameRule;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftGameRule;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.damage.CraftDamageEffect;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftMannequin;
// import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Pose;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.destroystokyo.paper.PaperSkinParts;
import com.destroystokyo.paper.SkinParts.Mutable;

@NullMarked
public class PaperServerInternalAPIBridge implements InternalAPIBridge {

    public static final PaperServerInternalAPIBridge INSTANCE = new PaperServerInternalAPIBridge();

    public DamageEffect getDamageEffect(String key) {
        return CraftDamageEffect.getById(key);
    }

    public Biome constructLegacyCustomBiome() {
        class Holder {
            static final Biome LEGACY_CUSTOM = new CraftBiome.LegacyCustomBiomeImpl();

            Holder(PaperServerInternalAPIBridge this$0) {
            }
        }
        return Holder.LEGACY_CUSTOM;
    }

    public CombatEntry createCombatEntry(org.bukkit.entity.LivingEntity entity, DamageSource damageSource, float damage) {
        LivingEntity mob = ((CraftLivingEntity)entity).getHandle();
        FallLocation fallLocation = FallLocation.getCurrentFallLocation(mob);
        return this.createCombatEntry(((CraftDamageSource)damageSource).getHandle(), damage, fallLocation, (float)mob.fallDistance);
    }

    public CombatEntry createCombatEntry(DamageSource damageSource, float damage, @Nullable FallLocationType fallLocationType, float fallDistance) {
        return this.createCombatEntry(((CraftDamageSource)damageSource).getHandle(), damage, Optionull.map(fallLocationType, PaperCombatTrackerWrapper::paperToMinecraft), fallDistance);
    }

    private CombatEntry createCombatEntry(net.minecraft.world.damagesource.DamageSource damageSource, float damage, @Nullable FallLocation fallLocation, float fallDistance) {
        return new PaperCombatEntryWrapper(new net.minecraft.world.damagesource.CombatEntry(damageSource, damage, fallLocation, fallDistance));
    }

    public Predicate<CommandSourceStack> restricted(Predicate<CommandSourceStack> predicate) {
        record RestrictedPredicate(Predicate<CommandSourceStack> predicate) implements Predicate<CommandSourceStack>
        // , PermissionLevelSource.RestrictedMarker
        {
            @Override
            public boolean test(CommandSourceStack commandSourceStack) {
                return this.predicate.test(commandSourceStack);
            }
        }
        return new RestrictedPredicate(predicate);
    }

	@Override
	public ResolvableProfile defaultMannequinProfile() {
		return new PaperResolvableProfile(Mannequin.DEFAULT_PROFILE);
	}

	// TODO: 1.21.9: Aw
	public static final byte MannequinEntity_ALL_MODEL_PARTS = (byte)Arrays.stream(PlayerModelPart.values())
		      .mapToInt(PlayerModelPart::getMask)
		      .reduce(0, (flagL, flagR) -> flagL | flagR);
	
	@Override
	public Mutable allSkinParts() {
		return new PaperSkinParts.Mutable(MannequinEntity_ALL_MODEL_PARTS);
	}

	@Override
	public Component defaultMannequinDescription() {
		// DEFAULT_DESCRIPTION not visible
		return PaperAdventure.asAdventure(net.minecraft.network.chat.Component.nullToEmpty("Hello, I'm a Mannequin"));
		// return PaperAdventure.asAdventure(MannequinEntity.DEFAULT_DESCRIPTION);
	}

	@Override
	public <MODERN, LEGACY> GameRule<LEGACY> legacyGameRuleBridge(
			GameRule<MODERN> rule, Function<LEGACY, MODERN> fromLegacyToModern, Function<MODERN, LEGACY> toLegacyFromModern, Class<LEGACY> legacyClass
			) {
		return CraftGameRule.wrap(rule, fromLegacyToModern, toLegacyFromModern, legacyClass);
	}

	@Override
	public Set<Pose> validMannequinPoses() {
		return CraftMannequin.VALID_POSES;
	}

}