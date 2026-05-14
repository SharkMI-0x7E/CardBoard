package io.papermc.paper.world.flag;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import java.util.Map.Entry;
import org.bukkit.FeatureFlag;
import org.bukkit.GameRule;
import org.bukkit.craftbukkit.CraftGameRule;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.potion.CraftPotionType;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

public class PaperFeatureFlagProviderImpl implements FeatureFlagProvider {
   public static final BiMap<FeatureFlag, net.minecraft.world.flag.FeatureFlag> FLAGS = ImmutableBiMap.of(
      FeatureFlag.MINECART_IMPROVEMENTS,
      FeatureFlags.MINECART_IMPROVEMENTS,
      FeatureFlag.REDSTONE_EXPERIMENTS,
      FeatureFlags.REDSTONE_EXPERIMENTS,
      FeatureFlag.TRADE_REBALANCE,
      FeatureFlags.TRADE_REBALANCE,
      FeatureFlag.VANILLA,
      FeatureFlags.VANILLA
   );

   public Set<FeatureFlag> requiredFeatures(FeatureDependant dependant) {
      FeatureFlagSet requiredFeatures = getFeatureElement(dependant).requiredFeatures();
      return fromNms(requiredFeatures);
   }

   public static Set<FeatureFlag> fromNms(FeatureFlagSet flagSet) {
      Set<FeatureFlag> flags = new HashSet<>();

      for (net.minecraft.world.flag.FeatureFlag nmsFlag : FeatureFlags.REGISTRY.names.values()) {
         if (flagSet.contains(nmsFlag)) {
            flags.add((FeatureFlag)FLAGS.inverse().get(nmsFlag));
         }
      }

      return Collections.unmodifiableSet(flags);
   }
   
   static FeatureElement getFeatureElement(FeatureDependant dependant) {
	   if (dependant instanceof EntityType entityType) {
		   return CraftEntityType.bukkitToMinecraft(entityType);
	   } else if (dependant instanceof PotionType potionType) {
		   return CraftPotionType.bukkitToMinecraft(potionType);
	   } else if (dependant instanceof GameRule<?> gameRule) {
		   return () -> CraftGameRule.bukkitToMinecraft(gameRule).requiredFeatures();
	   } else {
		   throw new IllegalArgumentException(dependant + " is not a valid feature dependant");
	   }
   }

   /*
   static ToggleableFeature getFeatureElement(FeatureDependant dependant) {
      if (dependant instanceof EntityType entityType) {
         return CraftEntityType.bukkitToMinecraft(entityType);
      } else if (dependant instanceof PotionType potionType) {
         return CraftPotionType.bukkitToMinecraft(potionType);
      } else if (dependant instanceof GameRule<?> gameRule) {
         return asFeatureElement( getGameRuleType(gameRule.getName()) ); //.asFeatureElement();
      } else {
         throw new IllegalArgumentException(dependant + " is not a valid feature dependant");
      }
   }

   // mixin into GameRules todo
   public static ToggleableFeature asFeatureElement(Type<?> type) {
	   return type::getRequiredFeatures;
   }

   private static GameRules.Type<?> getGameRuleType(String name) {
	   for (Entry<GameRules.Key<?>, GameRules.Type<?>> gameRules : GameRules.RULE_TYPES.entrySet()) {
		   if (gameRules.getKey().getName().equals(name)) {
			   return gameRules.getValue();
		   }
	   }

	   return null;
   }
   */
   
}
