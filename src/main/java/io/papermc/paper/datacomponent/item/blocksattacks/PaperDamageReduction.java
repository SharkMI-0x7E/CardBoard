package io.papermc.paper.datacomponent.item.blocksattacks;

import com.google.common.base.Preconditions;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.util.Conversions;
import io.papermc.paper.registry.set.PaperRegistrySets;
import io.papermc.paper.registry.set.RegistryKeySet;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.component.BlocksAttacks;
import org.bukkit.craftbukkit.util.Handleable;
import org.checkerframework.checker.index.qual.Positive;
import org.jspecify.annotations.Nullable;

public record PaperDamageReduction(BlocksAttacks.DamageReduction impl) implements DamageReduction,
Handleable<BlocksAttacks.DamageReduction>
{
    @Override
    public BlocksAttacks.DamageReduction getHandle() {
        return this.impl;
    }

    public @Nullable RegistryKeySet<org.bukkit.damage.DamageType> type() {
        return this.impl.type().map(set -> PaperRegistrySets.convertToApi(RegistryKey.DAMAGE_TYPE, set)).orElse(null);
    }

    public @Positive float horizontalBlockingAngle() {
        return this.impl.horizontalBlockingAngle();
    }

    public float base() {
        return this.impl.base();
    }

    public float factor() {
        return this.impl.factor();
    }

    static final class BuilderImpl
    implements DamageReduction.Builder {
        private Optional<HolderSet<DamageType>> type = Optional.empty();
        private float horizontalBlockingAngle = 90.0f;
        private float base = 0.0f;
        private float factor = 1.0f;

        BuilderImpl() {
        }

        public DamageReduction.Builder type(@Nullable RegistryKeySet<org.bukkit.damage.DamageType> type) {
            this.type = Optional.ofNullable(type).map(set -> PaperRegistrySets.convertToNms(Registries.DAMAGE_TYPE, Conversions.global().lookup(), set));
            return this;
        }

        public DamageReduction.Builder horizontalBlockingAngle(@Positive float horizontalBlockingAngle) {
            Preconditions.checkArgument((horizontalBlockingAngle > 0.0f ? 1 : 0) != 0, (String)"horizontalBlockingAngle must be positive and not zero, was %s", (Object)Float.valueOf(horizontalBlockingAngle));
            this.horizontalBlockingAngle = horizontalBlockingAngle;
            return this;
        }

        public DamageReduction.Builder base(float base) {
            this.base = base;
            return this;
        }

        public DamageReduction.Builder factor(float factor) {
            this.factor = factor;
            return this;
        }

        public DamageReduction build() {
            return new PaperDamageReduction(new BlocksAttacks.DamageReduction(this.horizontalBlockingAngle, this.type, this.base, this.factor));
        }
    }
}

