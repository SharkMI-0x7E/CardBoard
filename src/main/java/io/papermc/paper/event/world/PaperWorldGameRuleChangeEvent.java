package io.papermc.paper.event.world;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftGameRule;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PaperWorldGameRuleChangeEvent extends WorldGameRuleChangeEvent {

   public PaperWorldGameRuleChangeEvent(World world, @Nullable CommandSender commandSender, GameRule<?> gameRule, String value) {
      super(world, commandSender, gameRule, value);
   }

   public void setValue(String value) {
	   CraftGameRule craftRule = (CraftGameRule) this.gameRule;
	   net.minecraft.world.level.gamerules.GameRule<?> handle = (net.minecraft.world.level.gamerules.GameRule) craftRule.getHandle();

	   handle.deserialize(value).ifError(error -> {
		   throw new IllegalArgumentException("Invalid value: %s (%s)".formatted(value, error.message()));
	   });
	   super.setValue(value);
   }

}