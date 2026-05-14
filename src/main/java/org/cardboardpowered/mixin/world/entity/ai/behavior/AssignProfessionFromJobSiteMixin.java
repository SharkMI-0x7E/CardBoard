package org.cardboardpowered.mixin.world.entity.ai.behavior;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;

@MixinInfo(events = {"VillagerCareerChangeEvent"})
@Mixin(value = AssignProfessionFromJobSite.class, priority = 999)
public class AssignProfessionFromJobSiteMixin {

	/*
    @Redirect(method = "method_46891", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;setVillagerData(Lnet/minecraft/village/VillagerData;)V"))
    private static void banner$cancelJob(VillagerEntity instance, VillagerData villagerData) {}

    @Inject(method = "method_46891", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/passive/VillagerEntity;setVillagerData(Lnet/minecraft/village/VillagerData;)V"), cancellable = true)
    private static void banner$jobChange(VillagerEntity villagerEntity, ServerWorld serverLevel, VillagerProfession professn, RegistryEntry.Reference ref, CallbackInfo ci) {
        // CraftBukkit start - Fire VillagerCareerChangeEvent where Villager gets employed
        VillagerCareerChangeEvent event = CraftEventFactory.callVillagerCareerChangeEvent(villagerEntity, CraftVillager.nmsToBukkitProfession(profession), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
        if (event.isCancelled()) {
            ci.cancel();
        }

        villagerEntity.setVillagerData(villagerEntity.getVillagerData().withProfession(CraftVillager.CraftProfession.bukkitToMinecraftHolder(event.getProfession())));
        // CraftBukkit end
    }
    */
    
}