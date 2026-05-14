package org.cardboardpowered.mixin.network;

import java.net.SocketAddress;
import java.util.UUID;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.cardboardpowered.bridge.network.ConnectionBridge;
import com.mojang.authlib.properties.Property;

@Mixin(Connection.class)
public class ConnectionMixin implements ConnectionBridge {

    public UUID spoofedUUID;
    public Property[] spoofedProfile;
    public boolean preparing = true;

    @Override
    public SocketAddress getRawAddress() {
        return ((Connection)(Object)this).channel.remoteAddress();
    }

    @Override
    public UUID getSpoofedUUID() {
        return spoofedUUID;
    }

    @Override
    public void setSpoofedUUID(UUID uuid) {
        this.spoofedUUID = uuid;
    }

    @Override
    public Property[] getSpoofedProfile() {
        return spoofedProfile;
    }

    @Override
    public void setSpoofedProfile(Property[] profile) {
        this.spoofedProfile = profile;
    }

}
