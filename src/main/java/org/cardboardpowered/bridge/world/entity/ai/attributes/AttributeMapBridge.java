package org.cardboardpowered.bridge.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

public interface AttributeMapBridge {
    void cardboard$registerAttribute(Holder<Attribute> attributeBase);
}
