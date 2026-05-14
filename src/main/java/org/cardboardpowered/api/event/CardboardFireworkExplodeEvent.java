package org.cardboardpowered.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

public interface CardboardFireworkExplodeEvent {

    Event<CardboardFireworkExplodeEvent> EVENT = EventFactory.createArrayBacked(CardboardFireworkExplodeEvent.class,
            (listeners) -> (firework) -> {
                for (CardboardFireworkExplodeEvent listener : listeners) {
                    InteractionResult result = listener.interact(firework);

                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult interact(FireworkRocketEntity firework);
}
