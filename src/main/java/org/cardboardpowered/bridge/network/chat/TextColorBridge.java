package org.cardboardpowered.bridge.network.chat;

import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

public interface TextColorBridge {
    @Nullable ChatFormatting cardboard$getFormat();
}
