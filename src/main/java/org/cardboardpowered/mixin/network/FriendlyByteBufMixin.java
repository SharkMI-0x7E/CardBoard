/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufMixin {

    /**
     * @reason Set org.bukkit.item.ItemStack metadata
     */
	private void cb$todo() {
		
	}
	
    /*
	@Redirect(at = @At(value = "INVOKE", target="Lnet/minecraft/item/ItemStack;setNbt(Lnet/minecraft/nbt/NbtCompound;)V"), 
            method = { "readItemStack" })
    public void t(ItemStack stack, NbtCompound tag) {
        stack.setNbt(tag);
        if (stack.getNbt() != null) CraftItemStack.setItemMeta(stack, CraftItemStack.getItemMeta(stack));
    }
    */

    @Shadow
    public int readVarInt() {
        return 0;
    }

    @Shadow
    public byte readByte() {
        return 0;
    }

    @Shadow
    public CompoundTag readNbt() {
        return null;
    }

    @Shadow
    public boolean readBoolean() {
        return false;
    }

}