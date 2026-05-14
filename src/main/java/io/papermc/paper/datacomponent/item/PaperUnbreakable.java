package io.papermc.paper.datacomponent.item;

import org.bukkit.craftbukkit.util.Handleable;

@Deprecated
public record PaperUnbreakable() {
	
}

/*
public record PaperUnbreakable(
    net.minecraft.component.type.UnbreakableComponent impl
) implements Unbreakable, Handleable<net.minecraft.component.type.UnbreakableComponent> {

	
    @Override
    public boolean showInTooltip() {
        return this.impl.showInTooltip();
    }

    @Override
    public Unbreakable showInTooltip(final boolean showInTooltip) {
        return new PaperUnbreakable(this.impl.withShowInTooltip(showInTooltip));
    }

    @Override
    public net.minecraft.component.type.UnbreakableComponent getHandle() {
        return this.impl;
    }

    static final class BuilderImpl implements Unbreakable.Builder {

        private boolean showInTooltip = true;

        @Override
        public Unbreakable.Builder showInTooltip(final boolean showInTooltip) {
            this.showInTooltip = showInTooltip;
            return this;
        }

        @Override
        public Unbreakable build() {
            return new PaperUnbreakable(new net.minecraft.component.type.UnbreakableComponent(this.showInTooltip));
        }
    }
}*/
