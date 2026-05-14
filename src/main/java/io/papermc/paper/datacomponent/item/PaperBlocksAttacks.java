package io.papermc.paper.datacomponent.item;

import com.google.common.base.Preconditions;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;
import io.papermc.paper.datacomponent.item.blocksattacks.PaperDamageReduction;
import io.papermc.paper.datacomponent.item.blocksattacks.PaperItemDamageFunction;
import io.papermc.paper.registry.PaperRegistries;
import io.papermc.paper.registry.tag.TagKey;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.key.Key;
import net.minecraft.sounds.SoundEvent;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.damage.DamageType;
import org.jspecify.annotations.Nullable;

public record PaperBlocksAttacks(net.minecraft.world.item.component.BlocksAttacks impl) implements BlocksAttacks,
Handleable<net.minecraft.world.item.component.BlocksAttacks>
{
    @Override
    public net.minecraft.world.item.component.BlocksAttacks getHandle() {
        return this.impl;
    }

    public float blockDelaySeconds() {
        return this.impl.blockDelaySeconds();
    }

    public float disableCooldownScale() {
        return this.impl.disableCooldownScale();
    }

    public List<DamageReduction> damageReductions() {
        return this.impl.damageReductions().stream().map(PaperDamageReduction::new)
        		.map(paperDamageReduction -> (DamageReduction)paperDamageReduction).toList();
    }

    public ItemDamageFunction itemDamage() {
        return new PaperItemDamageFunction(this.impl.itemDamage());
    }

    public @Nullable TagKey<DamageType> bypassedBy() {
        Optional<TagKey> tagKey = this.impl.bypassedBy().map(PaperRegistries::fromNms);
        return tagKey.orElse(null);
    }

    public @Nullable Key blockSound() {
        return this.impl.blockSound().map(holder -> PaperAdventure.asAdventure(((SoundEvent)holder.value()).location())).orElse(null);
    }

    public @Nullable Key disableSound() {
        return this.impl.disableSound().map(holder -> PaperAdventure.asAdventure(((SoundEvent)holder.value()).location())).orElse(null);
    }

    static final class BuilderImpl
    implements BlocksAttacks.Builder {
        private float blockDelaySeconds;
        private float disableCooldownScale = 1.0f;
        private List<DamageReduction> damageReductions = new ObjectArrayList();
        private ItemDamageFunction itemDamage = new PaperItemDamageFunction(net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction.DEFAULT);
        private @Nullable TagKey<DamageType> bypassedBy;
        private @Nullable Key blockSound;
        private @Nullable Key disableSound;

        BuilderImpl() {
        }

        public BlocksAttacks.Builder blockDelaySeconds(float delay) {
            Preconditions.checkArgument((delay >= 0.0f ? 1 : 0) != 0, (String)"delay must be non-negative, was %s", (Object)Float.valueOf(delay));
            this.blockDelaySeconds = delay;
            return this;
        }

        public BlocksAttacks.Builder disableCooldownScale(float scale) {
            Preconditions.checkArgument((scale >= 0.0f ? 1 : 0) != 0, (String)"scale must be non-negative, was %s", (Object)Float.valueOf(scale));
            this.disableCooldownScale = scale;
            return this;
        }

        public BlocksAttacks.Builder addDamageReduction(DamageReduction reduction) {
            this.damageReductions.add(reduction);
            return this;
        }

        public BlocksAttacks.Builder damageReductions(List<DamageReduction> reductions) {
            this.damageReductions = new ObjectArrayList(reductions);
            return this;
        }

        public BlocksAttacks.Builder itemDamage(ItemDamageFunction function) {
            this.itemDamage = function;
            return this;
        }

        public BlocksAttacks.Builder bypassedBy(@Nullable TagKey<DamageType> bypassedBy) {
            this.bypassedBy = bypassedBy;
            return this;
        }

        public BlocksAttacks.Builder blockSound(@Nullable Key sound) {
            this.blockSound = sound;
            return this;
        }

        public BlocksAttacks.Builder disableSound(@Nullable Key sound) {
            this.disableSound = sound;
            return this;
        }

        public BlocksAttacks build() {
            return new PaperBlocksAttacks(new net.minecraft.world.item.component.BlocksAttacks(this.blockDelaySeconds, this.disableCooldownScale, this.damageReductions.stream().map(damageReduction -> ((PaperDamageReduction)damageReduction).getHandle()).toList(), ((PaperItemDamageFunction)this.itemDamage).getHandle(), Optional.ofNullable(this.bypassedBy).map(PaperRegistries::toNms), Optional.ofNullable(this.blockSound).map(PaperAdventure::resolveSound), Optional.ofNullable(this.disableSound).map(PaperAdventure::resolveSound)));
        }
    }
}

