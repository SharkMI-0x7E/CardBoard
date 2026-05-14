package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;

import org.cardboardpowered.TeleportTargetExtra;
import org.cardboardpowered.bridge.world.entity.EntityBridge;

// import io.papermc.paper.block.fluid.FluidData;
// import io.papermc.paper.block.fluid.PaperFluidData;
import io.papermc.paper.world.MoonPhase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.RegionAccessor;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.block.CraftBlock; 
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityTypes;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.SizedFireball;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public abstract class CraftRegionAccessor implements RegionAccessor {

    public abstract WorldGenLevel getHandle();

    public boolean isNormalWorld() {
        return this.getHandle() instanceof ServerLevel;
    }
	

    public Biome getBiome(Location location) {
        return this.getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Biome getBiome(int x, int y, int z) {
        return CraftBiome.minecraftHolderToBukkit(this.getHandle().getNoiseBiome(x >> 2, y >> 2, z >> 2));
    }

    public Biome getComputedBiome(int x, int y, int z) {
        return CraftBiome.minecraftHolderToBukkit(this.getHandle().getBiome(new BlockPos(x, y, z)));
    }

    public void setBiome(Location location, Biome biome) {
        this.setBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), biome);
    }

    public void setBiome(int x, int y, int z, Biome biome) {
        Preconditions.checkArgument((biome != Biome.CUSTOM ? 1 : 0) != 0, (String)"Cannot set the biome to %s", (Object)biome);
        Holder<net.minecraft.world.level.biome.Biome> biomeBase = CraftBiome.bukkitToMinecraftHolder(biome);
        this.setBiome(x, y, z, biomeBase);
    }

    public abstract void setBiome(int var1, int var2, int var3, Holder<net.minecraft.world.level.biome.Biome> var4);

    public BlockState getBlockState(Location location) {
        return this.getBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        return CraftBlock.at((ServerLevel) this.getHandle(), new BlockPos(x, y, z)).getState();
    }

    /*
    public FluidData getFluidData(int x, int y, int z) {
        return PaperFluidData.createData(this.getHandle().getFluidState(new BlockPos(x, y, z)));
    }
    */

    public BlockData getBlockData(Location location) {
        return this.getBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public BlockData getBlockData(int x, int y, int z) {
        return CraftBlockData.fromData(this.getData(x, y, z));
    }
    public Material getType(Location location) {
        return this.getType(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Material getType(int x, int y, int z) {
        return CraftBlockType.minecraftToBukkit(this.getData(x, y, z).getBlock());
    }

    protected net.minecraft.world.level.block.state.BlockState getData(int x, int y, int z) {
        return this.getHandle().getBlockState(new BlockPos(x, y, z));
    }

    public void setBlockData(Location location, BlockData blockData) {
        this.setBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockData);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData blockData) {
        WorldGenLevel world = this.getHandle();
        BlockPos pos = new BlockPos(x, y, z);
        net.minecraft.world.level.block.state.BlockState old = this.getHandle().getBlockState(pos);

        CraftBlock.setBlockState(world, pos, old, ((CraftBlockData) blockData).getState(), true);
    }

    public void setType(Location location, Material material) {
        this.setType(location.getBlockX(), location.getBlockY(), location.getBlockZ(), material);
    }

    public void setType(int x, int y, int z, Material material) {
        this.setBlockData(x, y, z, material.createBlockData());
    }

    public int getHighestBlockYAt(int x, int z) {
        return this.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING);
    }

    public int getHighestBlockYAt(Location location) {
        return this.getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
    }

    public int getHighestBlockYAt(int x, int z, HeightMap heightMap) {
        return this.getHandle().getHeight(CraftHeightMap.toNMS(heightMap), x, z);
    }

    public int getHighestBlockYAt(Location location, HeightMap heightMap) {
        return this.getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), heightMap);
    }

    public boolean generateTree(Location location, Random random, TreeType treeType) {
        BlockPos pos = CraftLocation.toBlockPosition(location);
        
        ChunkGenerator gen = ((ServerLevel)this.getHandle()).getChunkSource().getGenerator();
        
        return this.generateTree(this.getHandle(), gen, pos, new RandomSourceWrapper(random), treeType);
    }

    public boolean generateTree(Location location, Random random, TreeType treeType, Consumer<? super BlockState> consumer) {
        return this.generateTree(location, random, treeType, consumer == null ? null : block -> {
            consumer.accept((BlockState)block);
            return true;
        });
    }

    public boolean generateTree(Location location, Random random, TreeType treeType, Predicate<? super BlockState> predicate) {
        /*
    	BlockPos pos = CraftLocation.toBlockPosition(location);
        BlockStateListPopulator populator = new BlockStateListPopulator(this.getHandle());
        
        ChunkGenerator gen = ((ServerWorld)this.getHandle()).getChunkManager().getChunkGenerator()
        boolean result = this.generateTree(populator, gen, pos, new RandomSourceWrapper(random), treeType);
        populator.refreshTiles();
        for (BlockState blockState : populator.getList()) {
            if (predicate != null && !predicate.test((BlockState)blockState)) continue;
            blockState.update(true, true);
        }
        return result;
        */
    	// TODO
    	return false;
    }

    public boolean generateTree(WorldGenLevel access, ChunkGenerator chunkGenerator, BlockPos pos, net.minecraft.util.RandomSource random, TreeType treeType) {
        ResourceKey<ConfiguredFeature<?, ?>> gen;
        switch (treeType) {
            case BIG_TREE: {
                gen = TreeFeatures.FANCY_OAK;
                break;
            }
            case BIRCH: {
                gen = TreeFeatures.BIRCH;
                break;
            }
            case REDWOOD: {
                gen = TreeFeatures.SPRUCE;
                break;
            }
            case TALL_REDWOOD: {
                gen = TreeFeatures.PINE;
                break;
            }
            case JUNGLE: {
                gen = TreeFeatures.MEGA_JUNGLE_TREE;
                break;
            }
            case SMALL_JUNGLE: {
                gen = TreeFeatures.JUNGLE_TREE_NO_VINE;
                break;
            }
            case COCOA_TREE: {
                gen = TreeFeatures.JUNGLE_TREE;
                break;
            }
            case JUNGLE_BUSH: {
                gen = TreeFeatures.JUNGLE_BUSH;
                break;
            }
            case RED_MUSHROOM: {
                gen = TreeFeatures.HUGE_RED_MUSHROOM;
                break;
            }
            case BROWN_MUSHROOM: {
                gen = TreeFeatures.HUGE_BROWN_MUSHROOM;
                break;
            }
            case SWAMP: {
                gen = TreeFeatures.SWAMP_OAK;
                break;
            }
            case ACACIA: {
                gen = TreeFeatures.ACACIA;
                break;
            }
            case DARK_OAK: {
                gen = TreeFeatures.DARK_OAK;
                break;
            }
            case MEGA_REDWOOD: {
                gen = TreeFeatures.MEGA_PINE;
                break;
            }
            case TALL_BIRCH: {
                gen = TreeFeatures.SUPER_BIRCH_BEES_0002;
                break;
            }
            case CHORUS_PLANT: {
                ((ChorusFlowerBlock) Blocks.CHORUS_FLOWER).generatePlant(access, pos, random, 8);
                return true;
            }
            case CRIMSON_FUNGUS: {
                gen = this.isNormalWorld() ? TreeFeatures.CRIMSON_FUNGUS_PLANTED : TreeFeatures.CRIMSON_FUNGUS;
                break;
            }
            case WARPED_FUNGUS: {
                gen = this.isNormalWorld() ? TreeFeatures.WARPED_FUNGUS_PLANTED : TreeFeatures.WARPED_FUNGUS;
                break;
            }
            case AZALEA: {
                gen = TreeFeatures.AZALEA_TREE;
                break;
            }
            case MANGROVE: {
                gen = TreeFeatures.MANGROVE;
                break;
            }
            case TALL_MANGROVE: {
                gen = TreeFeatures.TALL_MANGROVE;
                break;
            }
            case CHERRY: {
                gen = TreeFeatures.CHERRY;
                break;
            }
            default: {
                gen = TreeFeatures.OAK;
            }
        }
        Holder<ConfiguredFeature<?, ?>> holder = access.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(gen).orElse(null);
        return holder != null ? (holder.value()).place(access, chunkGenerator, random, pos) : false;
    }

    public org.bukkit.entity.Entity spawnEntity(Location location, EntityType entityType) {
        return this.spawn(location, entityType.getEntityClass());
    }

    public org.bukkit.entity.Entity spawnEntity(Location loc, EntityType type, boolean randomizeData) {
        return this.spawn(loc, type.getEntityClass(), null, CreatureSpawnEvent.SpawnReason.CUSTOM, randomizeData);
    }

    public List<org.bukkit.entity.Entity> getEntities() {
        ArrayList<org.bukkit.entity.Entity> list = new ArrayList<org.bukkit.entity.Entity>();
        this.getNMSEntities().forEach(entity -> {
            CraftEntity bukkitEntity = ((EntityBridge)entity).getBukkitEntity();
            if (bukkitEntity != null && (!this.isNormalWorld() || bukkitEntity.isValid())) {
                list.add(bukkitEntity);
            }
        });
        return list;
    }

    public List<LivingEntity> getLivingEntities() {
        ArrayList<LivingEntity> list = new ArrayList<LivingEntity>();
        this.getNMSEntities().forEach(entity -> {
            CraftEntity bukkitEntity = ((EntityBridge)entity).getBukkitEntity();
            if (bukkitEntity != null && bukkitEntity instanceof LivingEntity && (!this.isNormalWorld() || bukkitEntity.isValid())) {
                list.add((LivingEntity)bukkitEntity);
            }
        });
        return list;
    }

    public <T extends org.bukkit.entity.Entity> Collection<T> getEntitiesByClass(Class<T> clazz) {
        ArrayList list = new ArrayList();
        this.getNMSEntities().forEach(entity -> {
            CraftEntity bukkitEntity = ((EntityBridge)entity).getBukkitEntity();
            if (bukkitEntity == null) {
                return;
            }
            Class bukkitClass = bukkitEntity.getClass();
            if (clazz.isAssignableFrom(bukkitClass) && (!this.isNormalWorld() || bukkitEntity.isValid())) {
                list.add(bukkitEntity);
            }
        });
        return list;
    }

    public Collection<org.bukkit.entity.Entity> getEntitiesByClasses(Class<?> ... classes) {
        ArrayList<org.bukkit.entity.Entity> list = new ArrayList<org.bukkit.entity.Entity>();
        this.getNMSEntities().forEach(entity -> {
            CraftEntity bukkitEntity = ((EntityBridge)entity).getBukkitEntity();
            if (bukkitEntity == null) {
                return;
            }
            Class bukkitClass = bukkitEntity.getClass();
            for (Class clazz : classes) {
                if (!clazz.isAssignableFrom(bukkitClass)) continue;
                if (this.isNormalWorld() && !bukkitEntity.isValid()) break;
                list.add(bukkitEntity);
                break;
            }
        });
        return list;
    }

    public abstract Iterable<Entity> getNMSEntities();

    public <T extends org.bukkit.entity.Entity> T createEntity(Location location, Class<T> clazz) throws IllegalArgumentException {
        Entity entity = this.createEntity(location, clazz, true);
        if (!this.isNormalWorld()) {
            // TODO
        	// entity.generation = true;
        }
        return (T)((EntityBridge)entity).getBukkitEntity();
    }

    public <T extends org.bukkit.entity.Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
        return this.spawn(location, clazz, null, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public <T extends org.bukkit.entity.Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> function) throws IllegalArgumentException {
        return this.spawn(location, clazz, function, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public <T extends org.bukkit.entity.Entity> T spawn(Location location, Class<T> clazz, boolean randomizeData, Consumer<? super T> function) throws IllegalArgumentException {
        return this.spawn(location, clazz, function, CreatureSpawnEvent.SpawnReason.CUSTOM, randomizeData);
    }

    public <T extends org.bukkit.entity.Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> function, CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException {
        return this.spawn(location, clazz, function, reason, true);
    }

    public <T extends org.bukkit.entity.Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> function, CreatureSpawnEvent.SpawnReason reason, boolean randomizeData) throws IllegalArgumentException {
        Entity entity = this.createEntity(location, clazz, randomizeData);
        return this.addEntity(entity, reason, function, randomizeData);
    }

    public <T extends org.bukkit.entity.Entity> T addEntity(T entity) {
        // Preconditions.checkArgument((!entity.isInWorld() ? 1 : 0) != 0, (Object)"Entity has already been added to a world");
        Entity nmsEntity = ((CraftEntity)entity).getHandle();
        if (nmsEntity.level() != this.getHandle().getLevel()) {
            nmsEntity = nmsEntity.teleport(TeleportTargetExtra.newTeleportTarget(this.getHandle().getLevel(), nmsEntity, TeleportTransition.DO_NOTHING));
        	// nmsEntity = nmsEntity.teleportTo(this.getHandle().toServerWorld());
        }
        this.addEntityWithPassengers(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (T)((EntityBridge)nmsEntity).getBukkitEntity();
    }

    public <T extends org.bukkit.entity.Entity> T addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException {
        return this.addEntity(entity, reason, null, true);
    }

    public <T extends org.bukkit.entity.Entity> T addEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason, Consumer<? super T> function, boolean randomizeData) throws IllegalArgumentException {
        Preconditions.checkArgument((entity != null ? 1 : 0) != 0, (Object)"Cannot spawn null entity");
        if (randomizeData && entity instanceof Mob) {
            ((Mob)entity).finalizeSpawn(this.getHandle(), this.getHandle().getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.COMMAND, null);
        }
        if (!this.isNormalWorld()) {
            // TODO entity.generation = true;
        }
        if (function != null) {
            function.accept((T) ((EntityBridge)entity).getBukkitEntity());
        }
        this.addEntityToWorld(entity, reason);
        return (T)((EntityBridge)entity).getBukkitEntity();
    }

    public abstract void addEntityToWorld(Entity var1, CreatureSpawnEvent.SpawnReason var2);

    public abstract void addEntityWithPassengers(Entity var1, CreatureSpawnEvent.SpawnReason var2);

    public Entity makeEntity(Location location, Class<? extends org.bukkit.entity.Entity> clazz) throws IllegalArgumentException {
        return this.createEntity(location, clazz, true);
    }

    // Note: Use old method till we fill CraftEntityTypes
	public abstract net.minecraft.world.entity.Entity createEntity_Old(Location location, Class<? extends org.bukkit.entity.Entity> clazz) throws IllegalArgumentException;

    
    public Entity createEntity(Location location, Class<? extends org.bukkit.entity.Entity> clazz_org, boolean randomizeData) throws IllegalArgumentException {
        Preconditions.checkArgument((location != null ? 1 : 0) != 0, (Object)"Location cannot be null");
        Preconditions.checkArgument((clazz_org != null ? 1 : 0) != 0, (Object)"Entity class cannot be null");
        
        Class<? extends org.bukkit.entity.Entity> clazz = clazz_org;
        
        Consumer<Entity> runOld = other -> {};
        if (clazz == AbstractArrow.class) {
            clazz = Arrow.class;
        } else if (clazz == AbstractHorse.class) {
            clazz = Horse.class;
        } else if (clazz == Fireball.class) {
            clazz = LargeFireball.class;
        } else if (clazz == LingeringPotion.class) {
            clazz = ThrownPotion.class;
            runOld = other -> ((AbstractThrownPotion)other).setItem(CraftItemStack.asNMSCopy(new ItemStack(Material.LINGERING_POTION, 1)));
        } else if (clazz == Minecart.class) {
            clazz = RideableMinecart.class;
        } else if (clazz == SizedFireball.class) {
            clazz = LargeFireball.class;
        } else if (clazz == SplashPotion.class) {
            clazz = ThrownPotion.class;
        } else if (clazz == TippedArrow.class) {
            clazz = Arrow.class;
            runOld = other -> ((Arrow)((EntityBridge)other).getBukkitEntity()).setBasePotionType(PotionType.WATER);
        }
        CraftEntityTypes.EntityTypeData entityTypeData = CraftEntityTypes.getEntityTypeData(clazz);
        if (entityTypeData == null || entityTypeData.spawnFunction() == null) {

        	try {
        		Entity entity = this.createEntity_Old(location, clazz_org);
        		if (entity != null) {
        			runOld.accept(entity);
        			return entity;
        		}
        	} catch (IllegalArgumentException e) {
        		// no
        	}
        	
        	
        	if (CraftEntity.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(String.format("Cannot spawn an entity from its CraftBukkit implementation class '%s' use the Bukkit class instead. You can get the Bukkit representation via Entity#getType()#getEntityClass()", clazz.getName()));
            }
            throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
        }
        // TODO
        /*
        if (!entityTypeData.entityType().isEnabledByFeature((World)((IMixinWorld)this.getHandle()).getCraftWorld())) {
            throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName() + " because it is not an enabled feature");
        }
        */
        Entity entity = (Entity)entityTypeData.spawnFunction().apply(new CraftEntityTypes.SpawnData(this.getHandle(), location, randomizeData, this.isNormalWorld()));
        
        
        
        if (entity != null) {
            runOld.accept(entity);
            return entity;
        }
        throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
    }

    public MoonPhase getMoonPhase() {
        return MoonPhase.getPhase(this.getHandle().getLevel().getDayTime() / 24000L);
    }

    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(this.getHandle().getLevel().dimension().identifier());
    }

    public boolean lineOfSightExists(Location from, Location to) {
        Preconditions.checkArgument((from != null ? 1 : 0) != 0, (Object)"from parameter in lineOfSightExists cannot be null");
        Preconditions.checkArgument((to != null ? 1 : 0) != 0, (Object)"to parameter in lineOfSightExists cannot be null");
        if (from.getWorld() != to.getWorld()) {
            return false;
        }
        Vec3 start = new Vec3(from.getX(), from.getY(), from.getZ());
        Vec3 end = new Vec3(to.getX(), to.getY(), to.getZ());
        if (end.distanceToSqr(start) > 16384.0) {
            return false;
        }
        return this.getHandle().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getType() == HitResult.Type.MISS;
    }

    public boolean hasCollisionsIn(@NotNull BoundingBox boundingBox) {
        AABB aabb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !this.getHandle().noCollision(aabb);
    }
}

