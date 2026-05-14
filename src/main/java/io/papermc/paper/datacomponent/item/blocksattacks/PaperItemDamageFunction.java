package io.papermc.paper.datacomponent.item.blocksattacks;

import com.google.common.base.Preconditions;
import net.minecraft.world.item.component.BlocksAttacks;
import org.bukkit.craftbukkit.util.Handleable;
import org.checkerframework.checker.index.qual.NonNegative;

public record PaperItemDamageFunction(BlocksAttacks.ItemDamageFunction impl) implements ItemDamageFunction,
Handleable<BlocksAttacks.ItemDamageFunction>
{
    @Override
    public BlocksAttacks.ItemDamageFunction getHandle() {
        return this.impl;
    }

    public @NonNegative float threshold() {
        return this.impl.threshold();
    }

    public float base() {
        return this.impl.base();
    }

    public float factor() {
        return this.impl.factor();
    }

    public int damageToApply(float damage) {
        return this.impl.apply(damage);
    }

    static final class BuilderImpl
    implements ItemDamageFunction.Builder {
        private float threshold = BlocksAttacks.ItemDamageFunction.DEFAULT.threshold();
        private float base = BlocksAttacks.ItemDamageFunction.DEFAULT.base();
        private float factor = BlocksAttacks.ItemDamageFunction.DEFAULT.factor();

        BuilderImpl() {
        }

        public ItemDamageFunction.Builder threshold(@NonNegative float threshold) {
            Preconditions.checkArgument((threshold >= 0.0f ? 1 : 0) != 0, (String)"threshold must be non-negative, was %s", (Object)Float.valueOf(threshold));
            this.threshold = threshold;
            return this;
        }

        public ItemDamageFunction.Builder base(float base) {
            this.base = base;
            return this;
        }

        public ItemDamageFunction.Builder factor(float factor) {
            this.factor = factor;
            return this;
        }

        public ItemDamageFunction build() {
            return new PaperItemDamageFunction(new BlocksAttacks.ItemDamageFunction(this.threshold, this.base, this.factor));
        }
    }
}

