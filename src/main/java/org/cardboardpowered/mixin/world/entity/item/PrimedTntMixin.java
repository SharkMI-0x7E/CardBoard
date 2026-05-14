package org.cardboardpowered.mixin.world.entity.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.cardboardpowered.bridge.world.entity.item.PrimedTntBridge;

@Mixin(PrimedTnt.class)
public class PrimedTntMixin implements PrimedTntBridge {

	/**
	 * @implNote LivingEntity (1.21.4) -> LazyEntityReference<LivingEntity> (1.21.8)
	 */
	@Shadow
	public EntityReference<LivingEntity> owner;

    @Override
    public void cardboard$setSource(LivingEntity entity) {
        this.owner = entity != null ? new EntityReference<LivingEntity>(entity) : null;;
    }

}
