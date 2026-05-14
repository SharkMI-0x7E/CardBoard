/**
 * Cardboard - Paper API for Fabric
 * Copyright (C) 2020-2025
 */
package org.cardboardpowered.bridge;

import net.minecraft.network.chat.Style;

// TODO
public interface IMixinStyle {

    Style setStrikethrough(Boolean obool);

    Style setUnderline(Boolean obool);

    Style setRandom(Boolean obool);

}