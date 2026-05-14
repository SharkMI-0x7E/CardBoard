package org.cardboardpowered.mixin.server;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Mixin(value=MinecraftServer.class)
public class MCServerMixin {

    // TODO: 1.18.2 @Shadow @Final public DynamicRegistryManager.Impl registryManager;
    @Shadow @Final public PlayerDataStorage playerDataStorage;

}