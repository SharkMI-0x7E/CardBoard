package io.papermc.paper.datacomponent.item.attribute;


import io.papermc.paper.datacomponent.item.attribute.AttributeModifierDisplay;
import io.papermc.paper.datacomponent.item.attribute.PaperDefaultDisplay;
import io.papermc.paper.datacomponent.item.attribute.PaperHiddenDisplay;
import io.papermc.paper.datacomponent.item.attribute.PaperOverrideTextDisplay;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.bukkit.craftbukkit.util.Handleable;

public interface PaperAttributeModifierDisplay<T extends ItemAttributeModifiers.Display>
extends Handleable<T> {
	
	static AttributeModifierDisplay fromNms(ItemAttributeModifiers.Display display) {
        return switch (display) {
            case ItemAttributeModifiers.Display.Default def -> new PaperDefaultDisplay(def);
            case ItemAttributeModifiers.Display.Hidden hidden -> new PaperHiddenDisplay(hidden);
            case ItemAttributeModifiers.Display.OverrideText override -> new PaperOverrideTextDisplay(override);
            default -> throw new UnsupportedOperationException("We do not know how to convert " + display.getClass());
        };
    }

    public static ItemAttributeModifiers.Display toNms(AttributeModifierDisplay display) {
        if (display instanceof PaperAttributeModifierDisplay) {
            PaperAttributeModifierDisplay modifierDisplay = (PaperAttributeModifierDisplay)display;
            return (ItemAttributeModifiers.Display)modifierDisplay.getHandle();
        }
        throw new UnsupportedOperationException("Must implement handleable!");
    }
}

