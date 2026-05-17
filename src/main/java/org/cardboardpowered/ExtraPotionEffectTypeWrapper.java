/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered;

import java.util.Map;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.jetbrains.annotations.NotNull;

public class ExtraPotionEffectTypeWrapper extends PotionEffectType {

	private int id;
	private NamespacedKey key;
	
	public ExtraPotionEffectTypeWrapper(int id, @NotNull String name) {
        // super(id, NamespacedKey.minecraft(name));
        
        this.id = id;
        this.key =  NamespacedKey.minecraft(name);
    }

    @Override
    public double getDurationModifier() {
        return getType().getDurationModifier();
    }

    @NotNull
    @Override
    public String getName() {
        return getType().getName();
    }

    /**
     * Get the potion type bound to this wrapper.
     *
     * @return The potion effect type
     */
    @NotNull
    public PotionEffectType getType() {
        return this;
    	//return PotionEffectType.getById(getId());
    }

    @Override
    public boolean isInstant() {
        return getType().isInstant();
    }

    @NotNull
    @Override
    public Color getColor() {
        return getType().getColor();
    }

	@Override
	public @NotNull String translationKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getAttributeModifierAmount(@NotNull Attribute arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @NotNull Map<Attribute, AttributeModifier> getEffectAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Category getEffectCategory() {
		// TODO Auto-generated method stub
		return Category.NEUTRAL;
	}

	@Override
	public @NotNull NamespacedKey getKey() {
		return this.key;
	}

	@Override
	public @NotNull PotionEffect createEffect(int duration, int amplifier) {
        return new PotionEffect(this, this.isInstant() ? 1 : (int) (duration * this.getDurationModifier()), amplifier);
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public @NotNull String getTranslationKey() {
		// TODO Auto-generated method stub
		return this.key.value();
	}

	@Override
	public @NotNull PotionEffectTypeCategory getCategory() {
		// TODO Auto-generated method stub
		return PotionEffectTypeCategory.NEUTRAL;
	}
}
