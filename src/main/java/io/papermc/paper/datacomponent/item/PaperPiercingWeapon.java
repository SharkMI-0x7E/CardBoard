package io.papermc.paper.datacomponent.item;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import org.bukkit.craftbukkit.util.Handleable;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record PaperPiercingWeapon(
    net.minecraft.world.item.component.PiercingWeapon impl
) implements PiercingWeapon, Handleable<net.minecraft.world.item.component.PiercingWeapon> {

    @Override
    public net.minecraft.world.item.component.PiercingWeapon getHandle() {
        return this.impl;
    }

    @Override
    public boolean dealsKnockback() {
        return this.impl.dealsKnockback();
    }

    @Override
    public boolean dismounts() {
        return this.impl.dismounts();
    }

    @Override
    public @Nullable Key sound() {
        return this.impl.sound()
            .map(Holder::value)
            .map(SoundEvent::location)
            .map(PaperAdventure::asAdventure)
            .orElse(null);
    }

    @Override
    public @Nullable Key hitSound() {
        return this.impl.hitSound()
            .map(Holder::value)
            .map(SoundEvent::location)
            .map(PaperAdventure::asAdventure)
            .orElse(null);
    }

    static final class BuilderImpl implements Builder {

        private boolean dealsKnockback = true;
        private boolean dismounts = false;

        private @Nullable Key sound = null;
        private @Nullable Key hitSound = null;

        @Override
        public Builder dealsKnockback(final boolean dealsKnockback) {
            this.dealsKnockback = dealsKnockback;
            return this;
        }

        @Override
        public Builder dismounts(final boolean dismounts) {
            this.dismounts = dismounts;
            return this;
        }

        @Override
        public Builder sound(final @Nullable Key sound) {
            this.sound = sound;
            return this;
        }

        @Override
        public Builder hitSound(final @Nullable Key sound) {
            this.hitSound = sound;
            return this;
        }

        @Override
        public PiercingWeapon build() {
            return new PaperPiercingWeapon(
                new net.minecraft.world.item.component.PiercingWeapon(
                    this.dealsKnockback,
                    this.dismounts,
                    Optional.ofNullable(this.sound).map(PaperAdventure::resolveSound),
                    Optional.ofNullable(this.hitSound).map(PaperAdventure::resolveSound)
                )
            );
        }
    }
}
