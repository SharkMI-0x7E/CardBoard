package org.cardboardpowered.mixin.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.cardboardpowered.bridge.world.entity.ai.attributes.AttributeMapBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(AttributeMap.class)
public class AttributeMapMixin implements AttributeMapBridge {
    @Shadow
    @Final
    private Map<Holder<Attribute>, AttributeInstance> attributes;

    // Paper - start - living entity allow attribute registration
    @Override
    public void cardboard$registerAttribute(Holder<Attribute> attributeBase) {
        AttributeInstance attributeModifiable = new AttributeInstance(attributeBase, AttributeInstance::getAttribute);
        attributes.put(attributeBase, attributeModifiable);
    }
    // Paper - end - living entity allow attribute registration
}
