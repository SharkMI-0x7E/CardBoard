/**
 * PaperWorldLoader
 */
package io.papermc.paper.world;

import com.google.common.io.Files;
import com.mojang.serialization.Dynamic;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.apache.commons.io.FileUtils;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.cardboardpowered.bridge.world.level.storage.LevelStorageSourceBridge;
import org.cardboardpowered.bridge.server.MinecraftServerBridge;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record PaperWorldLoader(MinecraftServer server, String levelId) {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Cardboard|PaperWorldLoader");// LogUtils.getClassLogger();

	public static PaperWorldLoader create(MinecraftServer server, String levelId) {
		return new PaperWorldLoader(server, levelId);
	}

	private PaperWorldLoader.WorldLoadingInfo getWorldInfo(String levelId, LevelStem stem) {
		ResourceKey<LevelStem> stemKey = this.server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM).getResourceKey(stem).orElseThrow();
		int dimension = 0;
		boolean enabled = true;

		// Cardboard: server.server -> CraftServer.INSTANCE
		if (stemKey == LevelStem.NETHER) {
			dimension = -1;
			enabled = CraftServer.INSTANCE.getAllowNether();
		} else if (stemKey == LevelStem.END) {
			dimension = 1;
			enabled = CraftServer.INSTANCE.getAllowEnd();
		} else if (stemKey != LevelStem.OVERWORLD) {
			dimension = -999;
		}

		String worldType = dimension == -999
				? stemKey.identifier().getNamespace() + "_" + stemKey.identifier().getPath()
						: Environment.getEnvironment(dimension).toString().toLowerCase(Locale.ROOT);
		String name = stemKey == LevelStem.OVERWORLD ? levelId : levelId + "_" + worldType;
		return new PaperWorldLoader.WorldLoadingInfo(dimension, name, worldType, stemKey, enabled);
	}
	
	public static Path LevelStorage_getStorageFolder(Path path, ResourceKey<LevelStem> dimensionType) {
		if (dimensionType == LevelStem.OVERWORLD) {
			return path;
		} else if (dimensionType == LevelStem.NETHER) {
			return path.resolve("DIM-1");
		} else {
			return dimensionType == LevelStem.END
					? path.resolve("DIM1")
							: path.resolve("dimensions").resolve(dimensionType.identifier().getNamespace()).resolve(dimensionType.identifier().getPath());
		}
	}

	private void migrateWorldFolder(PaperWorldLoader.WorldLoadingInfo info) {
		if (info.dimension() != 0) {
			File newWorld = LevelStorage_getStorageFolder(new File(info.name()).toPath(), info.stemKey()).toFile();
			File oldWorld = LevelStorage_getStorageFolder(new File(this.levelId).toPath(), info.stemKey()).toFile();
			File oldLevelDat = new File(new File(this.levelId), "level.dat");
			if (!newWorld.isDirectory() && oldWorld.isDirectory() && oldLevelDat.isFile()) {
				LOGGER.info("---- Migration of old " + info.worldType() + " folder required ----");
				LOGGER.info(
						"Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your "
								+ info.worldType()
								+ " folder to a new location in order to operate correctly."
						);
				LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
				LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");
				if (newWorld.exists()) {
					LOGGER.warn("A file or folder already exists at " + newWorld + "!");
					LOGGER.info("---- Migration of old " + info.worldType() + " folder failed ----");
				} else if (newWorld.getParentFile().mkdirs()) {
					if (oldWorld.renameTo(newWorld)) {
						LOGGER.info("Success! To restore " + info.worldType() + " in the future, simply move " + newWorld + " to " + oldWorld);

						try {
							Files.copy(oldLevelDat, new File(new File(info.name()), "level.dat"));
							FileUtils.copyDirectory(new File(new File(this.levelId), "data"), new File(new File(info.name()), "data"));
						} catch (IOException var6) {
							LOGGER.warn("Unable to migrate world data.");
						}

						LOGGER.info("---- Migration of old " + info.worldType() + " folder complete ----");
					} else {
						LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
						LOGGER.info("---- Migration of old " + info.worldType() + " folder failed ----");
					}
				} else {
					LOGGER.warn("Could not create path for " + newWorld + "!");
					LOGGER.info("---- Migration of old " + info.worldType() + " folder failed ----");
				}
			}
		}
	}

	public void loadInitialWorlds() {
		MinecraftServerBridge mc = (MinecraftServerBridge) this.server; // Cardboard
		
		for (LevelStem stem : this.server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM)) {
			PaperWorldLoader.WorldLoadingInfo info = this.getWorldInfo(this.levelId, stem);
			this.migrateWorldFolder(info);
			if (info.enabled()) {
				LevelStorageSource.LevelStorageAccess levelStorageAccess = ((MinecraftServerBridge) this.server).getSessionBF();
				if (info.dimension() != 0) {
					try {
						levelStorageAccess = ((LevelStorageSourceBridge) LevelStorageSource.createDefault(CraftServer.INSTANCE.getWorldContainer().toPath()))
								.validateAndCreateAccess(info.name(), info.stemKey());
					} catch (ContentValidationException | IOException var7) {
						throw new RuntimeException(var7);
					}
				}

				PaperWorldLoader.LevelDataResult levelData = getLevelData(levelStorageAccess);
				if (levelData.fatalError) {
					return;
				}

				PrimaryLevelData primaryLevelData;
				if (levelData.dataTag == null) {
					primaryLevelData = (PrimaryLevelData)Main.createNewWorldData(
							((DedicatedServer)this.server).settings,
							mc.cardboard$worldLoaderContext(),
							mc.cardboard$worldLoaderContext().datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM),
							this.server.isDemo(),
							true // TODO: this.server.options.has("bonusChest")
							)
							.cookie();
				} else {
					primaryLevelData = (PrimaryLevelData)LevelStorageSource.getLevelDataAndDimensions(
							levelData.dataTag,
							mc.cardboard$worldLoaderContext().dataConfiguration(),
							mc.cardboard$worldLoaderContext().datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM),
							mc.cardboard$worldLoaderContext().datapackWorldgen()
						)
							.worldData();
				}

				((PrimaryLevelDataBridge) primaryLevelData).checkName(info.name());
				primaryLevelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().shouldReportAsModified());

				/*
			if (this.server.options.has("forceUpgrade")) {
				Main.forceUpgradeWorld(
				  levelStorageAccess,
				  primaryLevelData,
				  Schemas.getFixer(),
				  this.server.options.has("eraseCache"),
				  () -> true,
				  this.server.getRegistryManager(),
				  this.server.options.has("recreateRegionFiles")
				);
			}
				 */

				((MinecraftServerBridge) this.server).createLevel(stem, info, levelStorageAccess, primaryLevelData);
			}
		}

		((DedicatedServer)this.server).forceDifficulty();

		for (ServerLevel serverLevel : this.server.getAllLevels()) {
			mc.cardboard$prepareLevel(serverLevel);
		}
	}

	public static PaperWorldLoader.LevelDataResult getLevelData(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		if (levelStorageAccess.hasWorldData()) {
			Dynamic<?> dataTag;
			LevelSummary summary;
			try {
				dataTag = levelStorageAccess.getDataTag();
				summary = levelStorageAccess.getSummary(dataTag);
			} catch (ReportedNbtException | IOException | NbtException var7) {
				LevelStorageSource.LevelDirectory levelDirectory = levelStorageAccess.getLevelDirectory();
				LOGGER.warn("Failed to load world data from {}", levelDirectory.dataFile(), var7);
				LOGGER.info("Attempting to use fallback");

				try {
					dataTag = levelStorageAccess.getDataTagFallback();
					summary = levelStorageAccess.getSummary(dataTag);
				} catch (ReportedNbtException | IOException | NbtException var6) {
					LOGGER.error("Failed to load world data from {}", levelDirectory.oldDataFile(), var6);
					LOGGER.error(
							"Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
							levelDirectory.dataFile(),
							levelDirectory.oldDataFile()
							);
					return new PaperWorldLoader.LevelDataResult(null, true);
				}

				levelStorageAccess.restoreLevelDataFromOld();
			}

			if (summary.requiresManualConversion()) {
				LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
				return new PaperWorldLoader.LevelDataResult(null, true);
			} else if (!summary.isCompatible()) {
				LOGGER.info("This world was created by an incompatible version.");
				return new PaperWorldLoader.LevelDataResult(null, true);
			} else {
				return new PaperWorldLoader.LevelDataResult(dataTag, false);
			}
		} else {
			return new PaperWorldLoader.LevelDataResult(null, false);
		}
	}

	public record LevelDataResult(@Nullable Dynamic<?> dataTag, boolean fatalError) {
	}

	public record WorldLoadingInfo(int dimension, String name, String worldType, ResourceKey<LevelStem> stemKey, boolean enabled) {
	}

}