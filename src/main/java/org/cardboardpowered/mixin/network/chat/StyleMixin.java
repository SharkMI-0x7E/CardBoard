package org.cardboardpowered.mixin.network.chat;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Style.class)
public class StyleMixin {

    @Shadow
    public Boolean bold;

    public Style setStrikethrough(Boolean obool) {
        Style st = ((Style)(Object)this).withBold(this.bold);
        st.strikethrough = obool;
        return st;
    }

    public Style setUnderline(Boolean obool) {
        return ((Style)(Object)this).withBold(this.bold).withUnderlined(obool);
    }

    public Style setRandom(Boolean obool) {
        Style st = ((Style)(Object)this).withBold(this.bold);
        st.obfuscated = obool;
        return st;
    }

}