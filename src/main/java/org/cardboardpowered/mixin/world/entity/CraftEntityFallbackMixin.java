package org.cardboardpowered.mixin.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftEntityTypes;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftEntity.class)
public class CraftEntityFallbackMixin {

    @Inject(method = "getEntity(Lorg/bukkit/craftbukkit/CraftServer;Lnet/minecraft/world/entity/Entity;)Lorg/bukkit/craftbukkit/entity/CraftEntity;", at = @At("HEAD"), cancellable = true)
    private static void cardboard$fallbackLivingEntity(CraftServer server, Entity entity, CallbackInfoReturnable<CraftEntity> cir) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        if (CraftEntityTypes.getEntityTypeData(CraftEntityType.minecraftToBukkit(entity.getType())) != null) {
            return;
        }

        cir.setReturnValue(new CraftLivingEntity(server, entity));
    }
}
