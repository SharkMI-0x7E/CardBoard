package org.bukkit.craftbukkit.generator;

import io.papermc.paper.world.flag.PaperFeatureFlagProviderImpl;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.bukkit.FeatureFlag;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.block.CraftBiome;
// import org.bukkit.craftbukkit.util.WorldUUID;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public class CraftWorldInfo implements WorldInfo {
   private final String name;
   private final UUID uuid;
   private final Environment environment;
   private final long seed;
   private final int minHeight;
   private final int maxHeight;
   private final FeatureFlagSet enabledFeatures;
   private final ChunkGenerator vanillaChunkGenerator;
   private final RegistryAccess.Frozen registryAccess;

   public CraftWorldInfo(
      PrimaryLevelData worldDataServer,
      LevelStorageSource.LevelStorageAccess session,
      Environment environment,
      DimensionType dimensionManager,
      ChunkGenerator chunkGenerator,
      RegistryAccess.Frozen registryAccess
   ) {
      this.registryAccess = registryAccess;
      this.vanillaChunkGenerator = chunkGenerator;
      this.name = worldDataServer.getLevelName();
      
      this.uuid = com.javazilla.bukkitfabric.Utils.getWorldUUID(session.levelDirectory.path().toFile());
      
      // this.uuid = WorldUUID.getOrCreate(session.getDirectory().path().toFile());
      this.environment = environment;
      this.seed = worldDataServer.worldGenOptions().seed();
      this.minHeight = dimensionManager.minY();
      this.maxHeight = dimensionManager.minY() + dimensionManager.height();
      this.enabledFeatures = worldDataServer.enabledFeatures();
   }

   public String getName() {
      return this.name;
   }

   public UUID getUID() {
      return this.uuid;
   }

   public Environment getEnvironment() {
      return this.environment;
   }

   public long getSeed() {
      return this.seed;
   }

   public int getMinHeight() {
      return this.minHeight;
   }

   public int getMaxHeight() {
      return this.maxHeight;
   }

   public BiomeProvider vanillaBiomeProvider() {
      final RandomState randomState;
      if (this.vanillaChunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
         randomState = RandomState.create(
            noiseBasedChunkGenerator.generatorSettings().value(), this.registryAccess.lookupOrThrow(Registries.NOISE), this.getSeed()
         );
      } else {
         randomState = RandomState.create(
            NoiseGeneratorSettings.dummy(), this.registryAccess.lookupOrThrow(Registries.NOISE), this.getSeed()
         );
      }

      final List<Biome> possibleBiomes = this.vanillaChunkGenerator
         .getBiomeSource()
         .possibleBiomes()
         .stream()
         .map(biome -> CraftBiome.minecraftHolderToBukkit((Holder<net.minecraft.world.level.biome.Biome>)biome))
         .toList();
      return new BiomeProvider() {
         public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return CraftBiome.minecraftHolderToBukkit(
               CraftWorldInfo.this.vanillaChunkGenerator.getBiomeSource().getNoiseBiome(x >> 2, y >> 2, z >> 2, randomState.sampler())
            );
         }

         public List<Biome> getBiomes(WorldInfo worldInfo) {
            return possibleBiomes;
         }
      };
   }

   public Set<FeatureFlag> getFeatureFlags() {
      return PaperFeatureFlagProviderImpl.fromNms(this.enabledFeatures);
   }
}
