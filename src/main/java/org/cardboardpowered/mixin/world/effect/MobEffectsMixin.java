package org.cardboardpowered.mixin.world.effect;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.effect.MobEffects;

@Mixin(MobEffects.class)
public class MobEffectsMixin {

    static {
        //for (Object effect : Registry.STATUS_EFFECT) {
       //     org.bukkit.potion.PotionEffectType.registerPotionEffectType(new CardboardPotionEffectType((StatusEffect) effect));
       // }
    }

}