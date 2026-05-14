package org.bukkit.craftbukkit;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

public class CraftFeatureFlag
implements FeatureFlag {
    private final NamespacedKey namespacedKey;
    private final net.minecraft.world.flag.FeatureFlag featureFlag;

    public CraftFeatureFlag(Identifier minecraftKey, net.minecraft.world.flag.FeatureFlag featureFlag) {
        this.namespacedKey = CraftNamespacedKey.fromMinecraft(minecraftKey);
        this.featureFlag = featureFlag;
    }

    public net.minecraft.world.flag.FeatureFlag getHandle() {
        return this.featureFlag;
    }

    @NotNull
    public NamespacedKey getKey() {
        return this.namespacedKey;
    }

    public String toString() {
        return "CraftDataPack{key=" + this.getKey() + ",keyUniverse=" + this.getHandle().universe.toString() + "}";
    }

    public static Set<CraftFeatureFlag> getFromNMS(FeatureFlagSet featureFlagSet) {
        HashSet<CraftFeatureFlag> set = new HashSet<CraftFeatureFlag>();
        FeatureFlags.REGISTRY.names.forEach((minecraftkey, featureflag) -> {
            if (featureFlagSet.contains((net.minecraft.world.flag.FeatureFlag)featureflag)) {
                set.add(new CraftFeatureFlag((Identifier)minecraftkey, (net.minecraft.world.flag.FeatureFlag)featureflag));
            }
        });
        return set;
    }

    public static CraftFeatureFlag getFromNMS(NamespacedKey namespacedKey) {
        return FeatureFlags.REGISTRY.names.entrySet().stream().filter(entry -> CraftNamespacedKey.fromMinecraft((Identifier)entry.getKey()).equals((Object)namespacedKey)).findFirst().map(entry -> new CraftFeatureFlag((Identifier)entry.getKey(), (net.minecraft.world.flag.FeatureFlag)entry.getValue())).orElse(null);
    }
}

