package org.cardboardpowered.mixin;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.compat.ModCompatibilityDatabase;
import org.cardboardpowered.compat.ModCompatibilityRule;
import org.cardboardpowered.library.Libraries;
import org.cardboardpowered.library.Library;
import org.cardboardpowered.library.LibraryManager;
import org.cardboardpowered.util.JarReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class CardboardMixinPlugin implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE_ROOT = "org.cardboardpowered.mixin.";
    private final Logger logger = LogManager.getLogger("Cardboard");
    public static boolean libload = true;
    private static boolean read_plugins = false;
    private static ModCompatibilityDatabase compatDatabase;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            CardboardConfig.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        File pl = new File("plugins");
        if (!pl.exists()) {
        	pl.mkdirs();
        }

        logger.info("Loading Libraries...");
        Libraries.loadLibs();
        JarReader.readEvents();
        
        if (CardboardConfig.autoConflictResolution) {
            compatDatabase = ModCompatibilityDatabase.load();
            compatDatabase.generateStartupReport();
        } else {
            logger.info("Automatic mod conflict resolution is disabled.");
        }
        
        if (pl.exists()) {
        	try {
                JarReader.readPlugins(pl);
                read_plugins = true;
            } catch (Exception e) {
                read_plugins = false;
                e.printStackTrace();
            }
        }
    }
    
    @Deprecated
    public static void loadLibs() {
    	Libraries.loadLibs();
    }
    
    @Deprecated
    public static List<Library> getLibs1() {
        return Libraries.getLibraries();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
        if (CardboardConfig.disabledMixins.contains(mixinClassName)) {
            logger.info("Disabling mixin '" + mixin + "', was forced disabled in config.");
            return false;
        }

        if (compatDatabase != null && CardboardConfig.autoConflictResolution) {
            for (String modId : compatDatabase.getLoadedKnownMods()) {
                ModCompatibilityRule rule = compatDatabase.getRuleForMod(modId).orElse(null);
                if (rule != null && rule.getDisabledMixins().contains(mixinClassName)) {
                    logger.info("Disabling mixin '" + mixin + "' due to compatibility rule for mod: " + rule.getModName());
                    return false;
                }
            }
        }

        if (mixin.equals("world.item.consume_effects.TeleportRandomlyConsumeEffectMixin")) {
            FabricLoader loader = FabricLoader.getInstance();
            boolean create_mod = loader.isModLoaded("porting_lib");
            if (create_mod) {
                return false;
            }
        }

        if (mixin.equals("server.network.ServerGamePacketListenerImplMixin_ChatEvent") &&
                should_force_alternate_chat()) {
            logger.info("Architectury Mod detected! Disabling async chat from NetworkHandler.");
            return false;
        }

        /*if (mixin.equals("network.MixinPlayerManager_ChatEvent")) {
            if (should_force_alternate_chat()) {
                logger.info("Architectury Mod detected! Using alternative async chat from PlayerManager");
                return true;
            } else return false;
        }
        if (CardboardConfig.ALT_CHAT && (mixin.contains("_ChatEvent"))) {
            logger.info("Alternative ChatEvent Mixin enabled in config. Changing status on: " + mixin);
            if (mixin.equals("network.MixinServerPlayNetworkHandler_ChatEvent")) return false;
            if (mixin.equals("network.MixinPlayerManager_ChatEvent")) return true;
        }*/

        // Disable mixin if event is not found in plugins.
        /*if (not_has_event(mixin, "LeashKnotEntity", "PlayerLeashEntityEvent") && not_has_event(mixin, "LeashKnotEntity", "PlayerUnleashEntityEvent")) return false;
        if (not_has_event(mixin, "GoToWorkTask", "VillagerCareerChangeEvent")) return false;
        if (not_has_event(mixin, "LoseJobOnSiteLossTask", "VillagerCareerChangeEvent")) return false;
        if (not_has_event(mixin, "PiglinBrain", "EntityPickupItemEvent")) return false;
        if (not_has_event(mixin, "DyeItem", "SheepDyeWoolEvent")) return false;
        if (not_has_event(mixin, "FrostWalkerEnchantment", "BlockFormEvent")) return false;
        if (not_has_event(mixin, "ExperienceOrbEntity", "PlayerItemMendEvent") || not_has_event(mixin, "ExperienceOrbEntity", "PlayerExpChangeEvent")) return false;
        if (not_has_event(mixin, "Explosion", "EntityExplodeEvent") && not_has_event(mixin, "Explosion", "BlockExplodeEvent")) return false;
        if (not_has_event(mixin, "LeavesBlock", "LeavesDecayEvent")) return false;
        if (not_has_event(mixin, "PlayerAdvancementTracker", "PlayerAdvancementDoneEvent")) return false;
*/
        if (mixinClassName.contains("ServerGamePacketListenerImpl")) return true;

        try {
            URLClassLoader ucl = getClassLoader();
            if (null == ucl) {
            	return true;
            }

            Class<?> c = Class.forName(mixinClassName, false, ucl);

            for (Annotation a : c.getAnnotations()) {
                String e = a.toString().split("events=")[1].substring(1);
                e = e.substring(0, e.lastIndexOf("}")).replace("\"", "");
                String[] events = e.split(", ");
                if (events.length > 0) {
                	// System.out.println("EVENTS: " + e);

                    if (events[0].length() < 4) {
                        return true; // No events
                    }

                    boolean disable = true;
                    for (String ev : events) {
                        if (!doesNotHaveEvent(mixin, mixin, ev))
                            disable = false;
                    }
                    //if (disable)
                    //    return false;
                }
            }

        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        return true;
    }
    
    public static URLClassLoader getClassLoader() throws MalformedURLException {
    	File papi = LibraryManager.INSTANCE.getJarFile("paper-api");
    	if (null == papi) {
    		return null;
    	}
        URL[] jar = {
                FabricLoader.getInstance().getModContainer("cardboard").get().getRootPath().toUri().toURL(),
                FabricLoader.getInstance().getModContainer("minecraft").get().getRootPath().toUri().toURL(),
                FabricLoader.getInstance().getModContainer("fabricloader").get().getRootPath().toUri().toURL(),
                papi.toURI().toURL()
        };
        return new URLClassLoader(jar);
    }
    
    public static boolean isEventFound(String event) {
        return read_plugins ? JarReader.found.contains(event) : true;
    }

    public boolean doesNotHaveEvent(String mix, String mixin, String event) {
        if (mix.contains(mixin)) {
            boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();
            boolean found = isEventFound(event);
            if (dev && !found) {logger.info("DEBUG: Status of " + mixin + ": " + found + ". (" + event + ")");}
            return !found;
        }
        return false;
    }

    /**
     * Check for mods that overwrite onGameMessage for chat event.
     */
    public boolean should_force_alternate_chat() {
        FabricLoader loader = FabricLoader.getInstance();
        String[] bad_mods = {"architectury", "dynmap"};

        for (String s : bad_mods) {
            if (loader.getModContainer(s).isPresent())
                return true;
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String target, ClassNode targetClass, String mixinClass, IMixinInfo info) {
    }

    @Override
    public void postApply(String targetClass, ClassNode target, String mixinClass, IMixinInfo info) {
    }

}