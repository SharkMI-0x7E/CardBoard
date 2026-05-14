package org.cardboardpowered.mixin.advancements;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.minecraft.advancements.DisplayInfo;
import org.cardboardpowered.bridge.advancements.DisplayInfoBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DisplayInfo.class)
public class DisplayInfoMixin implements DisplayInfoBridge {
    @Unique
    public final io.papermc.paper.advancement.AdvancementDisplay paper = new io.papermc.paper.advancement.PaperAdvancementDisplay((DisplayInfo) (Object) this); // Paper - Add more advancement API

    @Override
    public AdvancementDisplay cardboard$getPaper() {
        return this.paper;
    }
}