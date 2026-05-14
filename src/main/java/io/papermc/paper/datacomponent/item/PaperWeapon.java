package io.papermc.paper.datacomponent.item;

import com.google.common.base.Preconditions;
import net.minecraft.world.item.component.Weapon;
import org.bukkit.craftbukkit.util.Handleable;

public record PaperWeapon(Weapon impl) implements io.papermc.paper.datacomponent.item.Weapon, Handleable<Weapon> {

    @Override
    public Weapon getHandle() {
        return this.impl;
    }

    public int itemDamagePerAttack() {
        return this.impl.itemDamagePerAttack();
    }

    public float disableBlockingForSeconds() {
        return this.impl.disableBlockingForSeconds();
    }

    static final class BuilderImpl implements io.papermc.paper.datacomponent.item.Weapon.Builder {

        private int itemDamagePerAttack = 1;
        private float disableBlockingForSeconds;

        BuilderImpl() {
        }

        public io.papermc.paper.datacomponent.item.Weapon.Builder itemDamagePerAttack(int dam) {
            Preconditions.checkArgument(dam >= 0, "damage must >= 0, was " + dam);
            this.itemDamagePerAttack = dam;
            return this;
        }

        public io.papermc.paper.datacomponent.item.Weapon.Builder disableBlockingForSeconds(float sec) {
            Preconditions.checkArgument(sec >= 0.0f, "seconds must >= 0, was " + sec);
            this.disableBlockingForSeconds = sec;
            return this;
        }

        public io.papermc.paper.datacomponent.item.Weapon build() {
            return new PaperWeapon(new Weapon(this.itemDamagePerAttack, this.disableBlockingForSeconds));
        }

    }

}