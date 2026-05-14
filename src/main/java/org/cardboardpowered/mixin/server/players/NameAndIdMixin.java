package org.cardboardpowered.mixin.server.players;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.NameAndId;
import org.cardboardpowered.bridge.server.players.NameAndIdBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(NameAndId.class)
public class NameAndIdMixin implements NameAndIdBridge {
    @Shadow
    @Final
    private UUID id;

    @Shadow
    @Final
    private String name;

    // Paper start - utility method for common conversion back to the game profile
    @Override
    public GameProfile cardboard$toUncompletedGameProfile() {
        return new GameProfile(this.id, this.name);
    }
    // Paper end - utility method for common conversion back to the game profile
}
