package org.cardboardpowered.bridge.level.block.entity.trialspawner;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;

public interface TrialSpawner_FullConfigBridge {
    TrialSpawner.FullConfig cardboard$overrideTargetCooldownLength(int targetCooldownLength);

    TrialSpawner.FullConfig cardboard$overrideRequiredPlayerRange(int requiredPlayerRange);

    TrialSpawner.FullConfig cardboard$overrideConfigs(Holder<TrialSpawnerConfig> normal, Holder<TrialSpawnerConfig> ominous);
}
