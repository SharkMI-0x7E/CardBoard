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