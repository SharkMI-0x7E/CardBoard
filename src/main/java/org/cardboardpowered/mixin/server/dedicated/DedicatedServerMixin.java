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

	@Inject(at = @At(value = "JUMP", ordinal = 8), method = "initServer()Z")
	private void init(CallbackInfoReturnable<Boolean> ci) {

		// Register Bukkit Enchantments
		// for(Enchantment enchantment : Registries.ENCHANTMENT) {
		//     TODO: check for 1.20.3+
        //     TODO note: Do we really need this? like this TODO note as we can just uncomment this
		//     org.bukkit.enchantments.Enchantment.registerEnchantment(new CardboardEnchantment(enchantment));
		// }

		CardboardMagicNumbers.test();
		CardboardMagicNumbers.setupUnknownModdedMaterials();

		DedicatedServer server = (DedicatedServer) (Object) this;

		System.setProperty("bukkit.version", "Cardboard");

		server.setPlayerList(new DedicatedPlayerList(server, server.registries(), playerDataStorage));

		CraftServer craftServer = new CraftServer(server);

		Bukkit.setServer(craftServer);
		CraftServer.server = server;
		CraftServer.INSTANCE = craftServer;

		org.spigotmc.SpigotConfig.init(new File("spigot.yml"));

		Bukkit.getLogger().info("Loading Bukkit plugins...");
		File pluginsDir = new File("plugins");
		pluginsDir.mkdir();

		Bukkit.getPluginManager().registerInterface(JavaPluginLoader.class);

		craftServer.loadPlugins();
		craftServer.enablePlugins(PluginLoadOrder.STARTUP);

		Bukkit.getLogger().info("");
	}

	@Inject(at = @At("TAIL"), method = "onServerExit")
	public void killProcess(CallbackInfo ci) {
		BukkitLogger.getLogger().info("Goodbye!");
		// NOTE: do NOT call server.stopServer() here. MinecraftServer.runServer()
		// already ran stopServer() (with the Bukkit plugin shutdown + world save)
		// before onServerExit. Calling it again starts a SECOND save pass while the
		// chunk storage is already shutting down -> hangs forever on
		// "Saving chunks for level ... overworld" and the process never exits.
		//
		// Even a perfectly clean shutdown can leave non-daemon threads behind: our own pools,
		// and pools owned by plugins / shaded libraries (e.g. a plugin's bundled bStats creates
		// "bStats-Metrics"). The JVM only exits once EVERY non-daemon thread has finished, so we
		// must not depend on third parties being well-behaved. Report what is left, then exit
		// deterministically. See docs/ai/entries/BUG-019-non-daemon-pool-thread-blocks-jvm-exit.md
		java.util.List<String> lingering = new java.util.ArrayList<>();
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t == Thread.currentThread() || t.isDaemon() || !t.isAlive()) continue;
			// DestroyJavaVM is the JVM's own "wait for non-daemon threads" thread, not a leak.
			if ("DestroyJavaVM".equals(t.getName())) continue;
			lingering.add(t.getName());
		}
		if (!lingering.isEmpty()) {
			BukkitLogger.getLogger().warning("Shutdown report [mercy-watchdog build]: " + lingering.size()
					+ " non-daemon thread(s) still alive at shutdown: " + lingering);
		}
		// Arm a daemon "mercy watchdog" instead of calling System.exit():
		//  * a clean shutdown needs no help - the JVM exits on its own and this daemon thread simply
		//    dies with it (daemon threads never keep the JVM alive);
		//  * if anything holds the JVM back (lingering non-daemon threads - ours or a third party's -
		//    or a shutdown hook that blocks), halt unconditionally after a grace period.
		// We deliberately do NOT call System.exit() here: it runs shutdown hooks while holding the
		// Shutdown.class monitor, so if one hook blocks, EVERY later exit attempt - including Ctrl+C,
		// which also goes through System.exit() - blocks on that same monitor, and the process can
		// only be killed with "kill -9". halt() runs no hooks and cannot be blocked.
		// See docs/ai/entries/BUG-019-non-daemon-pool-thread-blocks-jvm-exit.md
		// How long to wait before forcing the exit depends on whether anything can still happen:
		//  * a surviving non-daemon thread means the JVM will NEVER initiate shutdown on its own,
		//    so no shutdown hook can be running either and waiting accomplishes nothing;
		//  * if nothing survives, let the normal path (and any shutdown hooks) finish - only keep a
		//    safety net for the case where something else blocks.
		final long graceMillis = lingering.isEmpty() ? 30000L : 2000L;
		Thread mercy = new Thread(() -> {
			try {
				Thread.sleep(graceMillis);
			} catch (InterruptedException ignored) {
				return;
			}
			BukkitLogger.getLogger().warning("JVM still alive " + (graceMillis / 1000L)
					+ "s after shutdown - forcing exit (halt).");
			Runtime.getRuntime().halt(0);
		}, "Cardboard Shutdown Watchdog");
		mercy.setDaemon(true);
		mercy.start();
	}

	/**
	 * @author BukkitFabric
	 * @reason ServerCommandEvent
	 */
	@Overwrite
	public void handleConsoleInputs() {
		while (!this.consoleInput.isEmpty()) {
			ConsoleInput servercommand = (ConsoleInput) this.consoleInput.remove(0);

			ServerCommandEvent event = new ServerCommandEvent(CraftServer.INSTANCE.getConsoleSender(), servercommand.msg);

			CraftServer.INSTANCE.getPluginManager().callEvent(event);

			if (event.isCancelled()) continue;

			servercommand = new ConsoleInput(event.getCommand(), servercommand.source);

			CraftServer.INSTANCE.dispatchServerCommand(
					CraftServer.INSTANCE.getConsoleSender(),
					servercommand
			);
		}
	}

	@Inject(method = "enforceSecureProfile", at = @At("HEAD"), cancellable = true)
	public void dontEnforceWithFix(CallbackInfoReturnable<Boolean> cir) {
		if (CardboardConfig.REGISTRY_COMMAND_FIX)
			cir.setReturnValue(false);
	}

	@Override
	public boolean isDebugging() {
		return false;
	}
}
