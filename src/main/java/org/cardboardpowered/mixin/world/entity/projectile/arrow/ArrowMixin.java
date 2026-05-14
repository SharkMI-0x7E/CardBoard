package org.cardboardpowered.mixin.world.entity.projectile.arrow;

import net.minecraft.world.entity.projectile.arrow.Arrow;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.world.entity.projectile.arrow.ArrowBridge;

@Mixin(Arrow.class)
public class ArrowMixin implements ArrowBridge {

    //@Shadow
    //public Potion potion;

    //@Shadow
    //public Set<StatusEffectInstance> effects;

    //@Shadow
    //private static TrackedData<Integer> COLOR;

    @Override
    public void setType(String string) {
        // TODO: 1.20.5
    	// this.potion = Registries.POTION.get(new Identifier(string));
        // (((Entity)(Object)this).getDataTracker()).set(COLOR, PotionUtil.getColor((Collection<StatusEffectInstance>) PotionUtil.getPotionEffects(this.potion, (Collection<StatusEffectInstance>) this.effects)));
    }

}
