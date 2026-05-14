/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.server.dedicated;

import org.cardboardpowered.BukkitLogger;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.bridge.server.dedicated.DedicatedServerBridge;
import org.cardboardpowered.impl.util.CardboardMagicNumbers;
import org.cardboardpowered.mixin.server.MCServerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.List;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin extends MCServerMixin implements DedicatedServerBridge {

	@Shadow
	@Final
	private List<ConsoleInput> consoleInput;

	@Inject(at = @At(value = "HEAD"), method = "initServer()Z")
	private void initVar(CallbackInfoReturnable<Boolean> callbackInfo) {
		CraftServer.server = (DedicatedServer) (Object) this;
	}

	@Inject(at = @At(value = "JUMP", ordinal = 8), method = "initServer()Z") // TODO keep ordinal updated
	private void init(CallbackInfoReturnable<Boolean> ci) {
		// Register Bukkit Enchantments
		// for(Enchantment enchantment : Registries.ENCHANTMENT) {
			// TODO: check for 1.20.3+
			// org.bukkit.enchantments.Enchantment.registerEnchantment(new CardboardEnchantment(enchantment));
		//}

		CardboardMagicNumbers.test();
		CardboardMagicNumbers.setupUnknownModdedMaterials();

		DedicatedServer thiss = (DedicatedServer) (Object) this;

		((DedicatedServer) (Object) this).setPlayerList(new DedicatedPlayerList(thiss, thiss.registries(), playerDataStorage));
		Bukkit.setServer(new CraftServer((DedicatedServer) (Object) this));
		org.spigotmc.SpigotConfig.init(new File("spigot.yml"));

		Bukkit.getLogger().info("Loading Bukkit plugins...");
		File pluginsDir = new File("plugins");
		pluginsDir.mkdir();

		Bukkit.getPluginManager().registerInterface(JavaPluginLoader.class);

		CraftServer s = ((CraftServer) Bukkit.getServer());
		if(CraftServer.server == null) CraftServer.server = (DedicatedServer) (Object) this;

		s.loadPlugins();
		s.enablePlugins(PluginLoadOrder.STARTUP);

		Bukkit.getLogger().info("");
	}

	@Inject(at = @At("TAIL"), method = "onServerExit")
	public void killProcess(CallbackInfo ci) {
		BukkitLogger.getLogger().info("Goodbye!");
		Runtime.getRuntime().halt(0);
	}

	/**
	 * @author BukkitFabric
	 * @reason ServerCommandEvent
	 */
	@Overwrite
	public void handleConsoleInputs() {
		while(!this.consoleInput.isEmpty()) {
			ConsoleInput servercommand = (ConsoleInput) this.consoleInput.remove(0);

			ServerCommandEvent event = new ServerCommandEvent(CraftServer.INSTANCE.getConsoleSender(), servercommand.msg);
			CraftServer.INSTANCE.getPluginManager().callEvent(event);
			if(event.isCancelled()) continue;
			servercommand = new ConsoleInput(event.getCommand(), servercommand.source);

			CraftServer.INSTANCE.dispatchServerCommand(CraftServer.INSTANCE.getConsoleSender(), servercommand);
		}
	}

	@Inject(method = "enforceSecureProfile", at = @At("HEAD"), cancellable = true)
	public void dontEnforceWithFix(CallbackInfoReturnable<Boolean> cir) {
		if(CardboardConfig.REGISTRY_COMMAND_FIX)
			cir.setReturnValue(false);
	}

	@Override
	public boolean isDebugging() {
		return false;
	}

}
