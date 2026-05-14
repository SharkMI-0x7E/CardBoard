package org.cardboardpowered.mixin.world.level.block.entity.trialspawner;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import org.cardboardpowered.bridge.level.block.entity.trialspawner.TrialSpawner_FullConfigBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TrialSpawner.FullConfig.class)
public class TrialSpawner_FullConfigMixin implements TrialSpawner_FullConfigBridge {
    @Shadow
    @Final
    Holder<TrialSpawnerConfig> normal;

    @Shadow
    @Final
    Holder<TrialSpawnerConfig> ominous;

    @Shadow
    @Final
    int targetCooldownLength;

    @Shadow
    @Final
    int requiredPlayerRange;

    // Paper start - trial spawner API - withers
    @Override
    public TrialSpawner.FullConfig cardboard$overrideTargetCooldownLength(final int targetCooldownLength) {
        return new TrialSpawner.FullConfig(
                this.normal,
                this.ominous,
                targetCooldownLength,
                this.requiredPlayerRange
        );
    }

    @Override
    public TrialSpawner.FullConfig cardboard$overrideRequiredPlayerRange(final int requiredPlayerRange) {
        return new TrialSpawner.FullConfig(
                this.normal,
                this.ominous,
                this.targetCooldownLength,
                requiredPlayerRange
        );
    }

    @Override
    public TrialSpawner.FullConfig cardboard$overrideConfigs(final Holder<TrialSpawnerConfig> normal, final Holder<TrialSpawnerConfig> ominous) {
        return new TrialSpawner.FullConfig(
                normal,
                ominous,
                this.targetCooldownLength,
                this.requiredPlayerRange
        );
    }
    // Paper end - trial spawner API - withers
}
