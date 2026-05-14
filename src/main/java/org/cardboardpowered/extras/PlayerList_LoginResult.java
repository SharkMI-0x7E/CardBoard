package org.cardboardpowered.extras;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public record PlayerList_LoginResult(@Nullable Component message, org.bukkit.event.player.PlayerLoginEvent.Result result) {
   public static PlayerList_LoginResult ALLOW = new PlayerList_LoginResult(null, org.bukkit.event.player.PlayerLoginEvent.Result.ALLOWED);

   public boolean isAllowed() {
      return this == ALLOW;
   }
}