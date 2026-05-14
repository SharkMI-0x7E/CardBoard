package org.cardboardpowered.mixin.bukkit.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.*;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.TimedRegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.cardboardpowered.CardboardConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.cardboardpowered.CardboardMod;

import io.papermc.paper.plugin.PermissionManager;
import io.papermc.paper.plugin.configuration.PluginMeta;

/**
 * @author cardboard
 * @reason Revert "009-Paper-Plugins.patch"
 */
@Mixin(value = SimplePluginManager.class, remap = false)
public class BukkitSimplePluginManagerMixin {

	@Shadow
	private Server server;

	@Shadow
	private File updateDirectory;

	@Shadow
	private Map<Pattern, PluginLoader> fileAssociations = new HashMap();
	@Shadow
	private List<Plugin> plugins = new ArrayList();
	@Shadow
	private Map<String, Plugin> lookupNames = new HashMap();
	
	
	@Shadow private MutableGraph<String> dependencyGraph = GraphBuilder.directed().build();

	@Shadow
	private SimpleCommandMap commandMap;
	@Shadow
	public Map<String, Permission> permissions = new HashMap();
	@Shadow
	public Map<Boolean, Set<Permission>> defaultPerms = new LinkedHashMap();
	@Shadow
	public Map<String, Map<Permissible, Boolean>> permSubs = new HashMap();
	@Shadow
	public Map<Boolean, Map<Permissible, Boolean>> defSubs = new HashMap();
	// public PluginManager paperPluginManager;
	// private boolean useTimings = false;

	@Inject(at = @At("HEAD"), method = "callEvent")
	public void cardboard$call_event_debug(Event event, CallbackInfo ci) {
		if (CardboardConfig.DEBUG_EVENT_CALL) {
			// Print debug info
			String name = event.getEventName();

			if (name.contains("EntityAirChangeEvent") || name.contains("EntityRemoveFromWorldEvent") || name.contains("EntityAddToWorldEvent")) {
				// Avoid spam
				return;
			}

			CardboardMod.LOGGER.info("debug: callEvent: " + event.getEventName());
		}
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public Plugin[] loadPlugins(File directory) {
		// Preconditions.checkArgument(directory != null, "Directory cannot be null");
		// Preconditions.checkArgument(directory.isDirectory(), "Directory must be a
		// directory");

		if (!(server.getUpdateFolder().equals(""))) {
			updateDirectory = new File(directory, server.getUpdateFolder());
		}

		return loadPlugins(directory.listFiles());
	}

	//@Shadow(remap = false)
	//public Plugin[] loadPlugins(File[] files) {
	//	return null;
	//}
	
	/**
	 * @reason .
	 * @author .
	 */
    public Plugin[] loadPlugins(File[] files) {
        //Preconditions.checkArgument(files != null, "File list cannot be null");

        List<Plugin> result = new ArrayList<Plugin>();
        Set<Pattern> filters = fileAssociations.keySet();

        Map<String, File> plugins = new HashMap<String, File>();
        Set<String> loadedPlugins = new HashSet<String>();
        Map<String, String> pluginsProvided = new HashMap<>();
        Map<String, Collection<String>> dependencies = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> softDependencies = new HashMap<String, Collection<String>>();

        // This is where it figures out all possible plugins
        for (File file : files) {
            PluginLoader loader = null;
            for (Pattern filter : filters) {
                Matcher match = filter.matcher(file.getName());
                if (match.find()) {
                    loader = fileAssociations.get(filter);
                }
            }

            if (loader == null) continue;

            PluginDescriptionFile description = null;
            try {
                description = loader.getPluginDescription(file);
                String name = description.getName();
                if (name.equalsIgnoreCase("bukkit") || name.equalsIgnoreCase("minecraft") || name.equalsIgnoreCase("mojang")) {
                    server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "': Restricted Name");
                    continue;
                } else if (description.getRawName().indexOf(' ') != -1) {
                    server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "': uses the space-character (0x20) in its name");
                    continue;
                }
            } catch (InvalidDescriptionException ex) {
                server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "'", ex);
                ex.printStackTrace();
                continue;
            }

            File replacedFile = plugins.put(description.getName(), file);
            if (replacedFile != null) {
                server.getLogger().severe(String.format(
                        "Ambiguous plugin name `%s' for files `%s' and `%s'",
                        description.getName(),
                        file.getPath(),
                        replacedFile.getPath()
                ));
            }

            String removedProvided = pluginsProvided.remove(description.getName());
            if (removedProvided != null) {
                server.getLogger().warning(String.format(
                        "Ambiguous plugin name `%s'. It is also provided by `%s'",
                        description.getName(),
                        removedProvided
                ));
            }

            for (String provided : description.getProvides()) {
                File pluginFile = plugins.get(provided);
                if (pluginFile != null) {
                    server.getLogger().warning(String.format(
                            "`%s provides `%s' while this is also the name of `%s'",
                            file.getPath(),
                            provided,
                            pluginFile.getPath()
                    ));
                } else {
                    String replacedPlugin = pluginsProvided.put(provided, description.getName());
                    if (replacedPlugin != null) {
                        server.getLogger().warning(String.format(
                                "`%s' is provided by both `%s' and `%s'",
                                provided,
                                description.getName(),
                                replacedPlugin
                        ));
                    }
                }
            }

            Collection<String> softDependencySet = description.getSoftDepend();
            if (softDependencySet != null && !softDependencySet.isEmpty()) {
                if (softDependencies.containsKey(description.getName())) {
                    // Duplicates do not matter, they will be removed together if applicable
                    softDependencies.get(description.getName()).addAll(softDependencySet);
                } else {
                    softDependencies.put(description.getName(), new LinkedList<String>(softDependencySet));
                }

                for (String depend : softDependencySet) {
                    dependencyGraph.putEdge(description.getName(), depend);
                }
            }

            Collection<String> dependencySet = description.getDepend();
            if (dependencySet != null && !dependencySet.isEmpty()) {
                dependencies.put(description.getName(), new LinkedList<String>(dependencySet));

                for (String depend : dependencySet) {
                    dependencyGraph.putEdge(description.getName(), depend);
                }
            }

            Collection<String> loadBeforeSet = description.getLoadBefore();
            if (loadBeforeSet != null && !loadBeforeSet.isEmpty()) {
                for (String loadBeforeTarget : loadBeforeSet) {
                    if (softDependencies.containsKey(loadBeforeTarget)) {
                        softDependencies.get(loadBeforeTarget).add(description.getName());
                    } else {
                        // softDependencies is never iterated, so 'ghost' plugins aren't an issue
                        Collection<String> shortSoftDependency = new LinkedList<String>();
                        shortSoftDependency.add(description.getName());
                        softDependencies.put(loadBeforeTarget, shortSoftDependency);
                    }

                    dependencyGraph.putEdge(loadBeforeTarget, description.getName());
                }
            }
        }

        while (!plugins.isEmpty()) {
            boolean missingDependency = true;
            Iterator<Map.Entry<String, File>> pluginIterator = plugins.entrySet().iterator();

            while (pluginIterator.hasNext()) {
                Map.Entry<String, File> entry = pluginIterator.next();
                String plugin = entry.getKey();

                if (dependencies.containsKey(plugin)) {
                    Iterator<String> dependencyIterator = dependencies.get(plugin).iterator();

                    while (dependencyIterator.hasNext()) {
                        String dependency = dependencyIterator.next();

                        // Dependency loaded
                        if (loadedPlugins.contains(dependency)) {
                            dependencyIterator.remove();

                        // We have a dependency not found
                        } else if (!plugins.containsKey(dependency) && !pluginsProvided.containsKey(dependency)) {
                            missingDependency = false;
                            pluginIterator.remove();
                            softDependencies.remove(plugin);
                            dependencies.remove(plugin);

                            server.getLogger().log(
                                    Level.SEVERE,
                                    "Could not load '" + entry.getValue().getPath() + "'",
                                    new UnknownDependencyException("Unknown dependency " + dependency + ". Please download and install " + dependency + " to run this plugin."));
                            break;
                        }
                    }

                    if (dependencies.containsKey(plugin) && dependencies.get(plugin).isEmpty()) {
                        dependencies.remove(plugin);
                    }
                }
                if (softDependencies.containsKey(plugin)) {
                    Iterator<String> softDependencyIterator = softDependencies.get(plugin).iterator();

                    while (softDependencyIterator.hasNext()) {
                        String softDependency = softDependencyIterator.next();

                        // Soft depend is no longer around
                        if (!plugins.containsKey(softDependency) && !pluginsProvided.containsKey(softDependency)) {
                            softDependencyIterator.remove();
                        }
                    }

                    if (softDependencies.get(plugin).isEmpty()) {
                        softDependencies.remove(plugin);
                    }
                }
                if (!(dependencies.containsKey(plugin) || softDependencies.containsKey(plugin)) && plugins.containsKey(plugin)) {
                    // We're clear to load, no more soft or hard dependencies left
                    File file = plugins.get(plugin);
                    pluginIterator.remove();
                    missingDependency = false;

                    try {
                        Plugin loadedPlugin = loadPlugin(file);
                        if (loadedPlugin != null) {
                            result.add(loadedPlugin);
                            loadedPlugins.add(loadedPlugin.getName());
                            loadedPlugins.addAll(loadedPlugin.getDescription().getProvides());
                        } else {
                            server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "'");
                        }
                        continue;
                    } catch (InvalidPluginException ex) {
                        server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "'", ex);
                        ex.printStackTrace();
                    }
                }
            }

            if (missingDependency) {
                // We now iterate over plugins until something loads
                // This loop will ignore soft dependencies
                pluginIterator = plugins.entrySet().iterator();

                while (pluginIterator.hasNext()) {
                    Map.Entry<String, File> entry = pluginIterator.next();
                    String plugin = entry.getKey();

                    if (!dependencies.containsKey(plugin)) {
                        softDependencies.remove(plugin);
                        missingDependency = false;
                        File file = entry.getValue();
                        pluginIterator.remove();

                        try {
                            Plugin loadedPlugin = loadPlugin(file);
                            if (loadedPlugin != null) {
                                result.add(loadedPlugin);
                                loadedPlugins.add(loadedPlugin.getName());
                                loadedPlugins.addAll(loadedPlugin.getDescription().getProvides());
                            } else {
                                server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "'");
                            }
                            break;
                        } catch (InvalidPluginException ex) {
                            server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "'", ex);
                            ex.printStackTrace();
                        }
                    }
                }
                // We have no plugins left without a depend
                if (missingDependency) {
                    softDependencies.clear();
                    dependencies.clear();
                    Iterator<File> failedPluginIterator = plugins.values().iterator();

                    while (failedPluginIterator.hasNext()) {
                        File file = failedPluginIterator.next();
                        failedPluginIterator.remove();
                        server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "': circular dependency detected");
                    }
                }
            }
        }

        return result.toArray(new Plugin[result.size()]);
    }

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public synchronized Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
		// Preconditions.checkArgument(file != null, "File cannot be null");

		checkUpdate(file);

		Set<Pattern> filters = fileAssociations.keySet();
		Plugin result = null;

		for (Pattern filter : filters) {
			String name = file.getName();
			Matcher match = filter.matcher(name);

			if (match.find()) {
				PluginLoader loader = fileAssociations.get(filter);

				result = loader.loadPlugin(file);
			}
		}

		if (result != null) {
			plugins.add(result);
			lookupNames.put(result.getDescription().getName(), result);
			for (String provided : result.getDescription().getProvides()) {
				lookupNames.putIfAbsent(provided, result);
			}
		}

		return result;
	}

	@Shadow
	private void checkUpdate(File file) {
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public synchronized Plugin getPlugin( String name) {
		return lookupNames.get(name.replace(' ', '_'));
		// return this.paperPluginManager.getPlugin(name);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public synchronized Plugin[] getPlugins() {
		return plugins.toArray(new Plugin[plugins.size()]);
		// return this.paperPluginManager.getPlugins();
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public boolean isPluginEnabled( String name) {
        Plugin plugin = getPlugin(name);

        return isPluginEnabled(plugin);
		
		// return this.paperPluginManager.isPluginEnabled(name);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public boolean isPluginEnabled(Plugin plugin) {
        if ((plugin != null) && (plugins.contains(plugin))) {
            return plugin.isEnabled();
        } else {
            return false;
        }
		
		// return this.paperPluginManager.isPluginEnabled(plugin);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void enablePlugin( Plugin plugin) {
        if (!plugin.isEnabled()) {
            List<Command> pluginCommands = PluginCommandYamlParser.parse(plugin);

            if (!pluginCommands.isEmpty()) {
                commandMap.registerAll(plugin.getDescription().getName(), pluginCommands);
            }

            try {
                plugin.getPluginLoader().enablePlugin(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            HandlerList.bakeAll();
        }
		
		// this.paperPluginManager.enablePlugin(plugin);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void disablePlugins() {
        Plugin[] plugins = getPlugins();
        for (int i = plugins.length - 1; i >= 0; i--) {
            disablePlugin(plugins[i]);
        }
		
		// this.paperPluginManager.disablePlugins();
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void disablePlugin( Plugin plugin) {
		if (plugin.isEnabled()) {
            try {
                plugin.getPluginLoader().disablePlugin(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                server.getScheduler().cancelTasks(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                server.getServicesManager().unregisterAll(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering services for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                HandlerList.unregisterAll(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering events for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                server.getMessenger().unregisterIncomingPluginChannel(plugin);
                server.getMessenger().unregisterOutgoingPluginChannel(plugin);
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering plugin channels for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }

            try {
                for (World world : server.getWorlds()) {
                    world.removePluginChunkTickets(plugin);
                }
            } catch (Throwable ex) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while removing chunk tickets for " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
            }
        }
		
		// this.paperPluginManager.disablePlugin(plugin);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void clearPlugins() {
        synchronized ((SimplePluginManager) (Object) this ) {
            disablePlugins();
            plugins.clear();
            lookupNames.clear();
            // dependencyGraph = GraphBuilder.directed().build();
            HandlerList.unregisterAll();
            fileAssociations.clear();
            permissions.clear();
            defaultPerms.get(true).clear();
            defaultPerms.get(false).clear();
        }
		
		// this.paperPluginManager.clearPlugins();
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void callEvent( Event event) {
        if (event.isAsynchronous()) {
            if (Thread.holdsLock(this)) {
                throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from inside synchronized code.");
            }
            if (server.isPrimaryThread()) {
                throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from primary server thread.");
            }
        } else {
            if (!server.isPrimaryThread()) {
                throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from another thread.");
            }
        }

        fireEvent(event);
		
		//this.paperPluginManager.callEvent(event);
	}
	
	@Shadow
	private void fireEvent(Event event) {}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void registerEvents( Listener listener,  Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }

        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
            getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
        }
		
		// this.paperPluginManager.registerEvents(listener, plugin);
	}
	
	@Shadow
	private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
		return null;
	}
	
	@Shadow
	private HandlerList getEventListeners(Class<? extends Event> type) {
		return null;
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void registerEvent( Class<? extends Event> event,  Listener listener,
			 EventPriority priority,  EventExecutor executor,  Plugin plugin,
			boolean ignoreCancelled) {
		// Preconditions.checkArgument(listener != null, "Listener cannot be null");
		// Preconditions.checkArgument(priority != null, "Priority cannot be null");
		// Preconditions.checkArgument(executor != null, "Executor cannot be null");
		// Preconditions.checkArgument(plugin != null, "Plugin cannot be null");
		
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + event + " while not enabled");
        }

        if (this.useTimings()) {
            getEventListeners(event).register(new TimedRegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
        } else {
            getEventListeners(event).register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
        }
		
		// this.paperPluginManager.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public Permission getPermission( String name) {
		 return permissions.get(name.toLowerCase(Locale.ROOT));
		//return this.paperPluginManager.getPermission(name);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void addPermission( Permission perm) {
		 addPermission(perm, true);
	}

	/**
	 * @reason cardboard
	 * @author cardboard
	 */
	@Overwrite(remap = false)
	public void addPermission( Permission perm, boolean dirty) {
        String name = perm.getName().toLowerCase(Locale.ROOT);

        if (permissions.containsKey(name)) {
            throw new IllegalArgumentException("The permission " + name + " is already defined!");
        }

        permissions.put(name, perm);
        calculatePermissionDefault(perm, dirty);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public Set<Permission> getDefaultPermissions(boolean op) {
		return ImmutableSet.copyOf(defaultPerms.get(op));
	}
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
    public void removePermission(Permission perm) {
        removePermission(perm.getName());
    }
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
    public void removePermission(String name) {
        permissions.remove(name.toLowerCase(Locale.ROOT));
    }
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void recalculatePermissionDefaults( Permission perm) {
        if (perm != null && permissions.containsKey(perm.getName().toLowerCase(Locale.ROOT))) {
            defaultPerms.get(true).remove(perm);
            defaultPerms.get(false).remove(perm);

            calculatePermissionDefault(perm, true);
        }
	}
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
    private void calculatePermissionDefault(Permission perm, boolean dirty) {
        if ((perm.getDefault() == PermissionDefault.OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get(true).add(perm);
            if (dirty) {
                dirtyPermissibles(true);
            }
        }
        if ((perm.getDefault() == PermissionDefault.NOT_OP) || (perm.getDefault() == PermissionDefault.TRUE)) {
            defaultPerms.get(false).add(perm);
            if (dirty) {
                dirtyPermissibles(false);
            }
        }
    }
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
    public void dirtyPermissibles() {
        dirtyPermissibles(true);
        dirtyPermissibles(false);
    }
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
    private void dirtyPermissibles(boolean op) {
        Set<Permissible> permissibles = getDefaultPermSubscriptions(op);

        for (Permissible p : permissibles) {
            p.recalculatePermissions();
        }
    }
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void subscribeToPermission( String permission,  Permissible permissible) {
        String name = permission.toLowerCase(Locale.ROOT);
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map == null) {
            map = new WeakHashMap<Permissible, Boolean>();
            permSubs.put(name, map);
        }

        map.put(permissible, true);
	}
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void unsubscribeFromPermission( String permission,  Permissible permissible) {
        String name = permission.toLowerCase(Locale.ROOT);
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                permSubs.remove(name);
            }
        }
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public Set<Permissible> getPermissionSubscriptions( String permission) {
        String name = permission.toLowerCase(Locale.ROOT);
        Map<Permissible, Boolean> map = permSubs.get(name);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
	}
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void subscribeToDefaultPerms(boolean op,  Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op);

        if (map == null) {
            map = new WeakHashMap<Permissible, Boolean>();
            defSubs.put(op, map);
        }

        map.put(permissible, true);
	}
	
	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void unsubscribeFromDefaultPerms(boolean op,  Permissible permissible) {
        Map<Permissible, Boolean> map = defSubs.get(op);

        if (map != null) {
            map.remove(permissible);

            if (map.isEmpty()) {
                defSubs.remove(op);
            }
        }
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        Map<Permissible, Boolean> map = defSubs.get(op);

        if (map == null) {
            return ImmutableSet.of();
        } else {
            return ImmutableSet.copyOf(map.keySet());
        }
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public Set<Permission> getPermissions() {
		 return new HashSet<Permission>(permissions.values());
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public boolean useTimings() {
		return false;
		// return this.paperPluginManager.useTimings();
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void clearPermissions() {
		//this.paperPluginManager.clearPermissions();
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public boolean isTransitiveDependency(PluginMeta pluginMeta, PluginMeta dependencyConfig) {
		//return this.paperPluginManager.isTransitiveDependency(pluginMeta, dependencyConfig);
		return false;
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void overridePermissionManager( Plugin plugin,  PermissionManager permissionManager) {
		//this.paperPluginManager.overridePermissionManager(plugin, permissionManager);
	}

	/**
	 * @reason .
	 * @author .
	 */
	@Overwrite(remap = false)
	public void addPermissions( List<Permission> perm) {
		
		for (Permission p : perm)
			this.addPermission(p);
		
		//this.paperPluginManager.addPermissions(perm);
	}

}
