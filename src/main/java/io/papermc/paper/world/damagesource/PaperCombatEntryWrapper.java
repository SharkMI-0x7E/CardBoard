package io.papermc.paper.world.damagesource;

import io.papermc.paper.world.damagesource.CombatEntry;
import io.papermc.paper.world.damagesource.FallLocationType;
import io.papermc.paper.world.damagesource.PaperCombatTrackerWrapper;
import net.minecraft.Optionull;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.damage.DamageSource;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record PaperCombatEntryWrapper(net.minecraft.world.damagesource.CombatEntry handle) implements CombatEntry {

    public DamageSource getDamageSource() {
        return new CraftDamageSource(this.handle.source());
    }

    public float getDamage() {
        return this.handle.damage();
    }

    public @Nullable FallLocationType getFallLocationType() {
        return Optionull.map(this.handle.fallLocation(), PaperCombatTrackerWrapper::minecraftToPaper);
    }

    public float getFallDistance() {
        return this.handle.fallDistance();
    }

}