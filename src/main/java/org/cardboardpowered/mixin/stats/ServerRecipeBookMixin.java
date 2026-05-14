package org.cardboardpowered.mixin.stats;

import net.minecraft.stats.ServerRecipeBook;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin {

	/*
    @Inject(at = @At("HEAD"), method = "sendUnlockRecipesPacket", cancellable = true)
    private void dontSendPacketBeforeLogin(ChangeUnlockedRecipesS2CPacket.Action packetplayoutrecipes_action, ServerPlayerEntity entityplayer, List<Identifier> list, CallbackInfo ci) {
        // See SPIGOT-4478
        if (entityplayer.networkHandler == null)
            ci.cancel();
    }
    */

}