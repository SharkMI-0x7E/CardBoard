package org.cardboardpowered.mixin.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

@Mixin(value = Bukkit.class, remap = false)
public class BukkitMixin {

	/**
	 * Use Fabric's ModMetadata for version
	 * info instead of grabbing from META-INF
	 * 
	 * @author cardboard
	 * @reason META-INF
	 *
	 * TODO: Cannot replace with @ModifyReturnValue - this method completely replaces
	 * the original Bukkit logic to use Fabric ModMetadata instead of META-INF.
	 */
	@Overwrite(remap = false)
    public static String getVersionMessage() {
		ModMetadata metadata = FabricLoader.getInstance().getModContainer("cardboard").get().getMetadata();
		
		String ver = metadata.getVersion().getFriendlyString();
        if (ver.contains("version")) ver = CraftServer.INSTANCE.getShortVersion(); // Dev ENV
		
		return "This server is running " + Bukkit.getName() + " version " + ver + " (Implementing API version " + Bukkit.getBukkitVersion() + ")";
    }
	
}
