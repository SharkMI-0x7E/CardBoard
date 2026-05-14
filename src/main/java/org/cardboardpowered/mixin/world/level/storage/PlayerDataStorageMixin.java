package org.cardboardpowered.mixin.world.level.storage;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.world.level.storage.PlayerDataStorageBridge;
import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.FileInputStream;

@Mixin(value = PlayerDataStorage.class, priority = 999)
public class PlayerDataStorageMixin implements PlayerDataStorageBridge {

    @Shadow
    @Final
    private File playerDir;

    @Shadow
    @Final
    protected DataFixer fixerUpper;

    
    /**
     * @reason offline uuid
     * @author cardboard mod
     */
   /* @Overwrite
    public Optional<NbtCompound> loadPlayerData(PlayerEntity player) {
        return this.load(player.getName().getString(), player.getUuidAsString()).map(nbttagcompound -> {
            if (player instanceof ServerPlayerEntity) {
                CraftPlayer player1 = (CraftPlayer)player.getBukkitEntity();
                long modified = new File(this.playerDataDir, player.getUuidAsString() + ".dat").lastModified();
                if (modified < player1.getFirstPlayed()) {
                    player1.setFirstPlayed(modified);
                }
            }
            player.readNbt((NbtCompound)nbttagcompound);
            return nbttagcompound;
        });
    }
    
    public Optional<NbtCompound> load(String name, String uuid) {
        Optional<NbtCompound> optional = this.load(name, uuid, ".dat");
        if (optional.isEmpty()) {
            this.backup(name, uuid, ".dat");
        }
        return optional.or(() -> this.load(name, uuid, ".dat_old")).map(nbttagcompound -> {
            int i2 = NbtHelper.getDataVersion(nbttagcompound, -1);
            nbttagcompound = MCDataConverter.convertTag(MCTypeRegistry.PLAYER, nbttagcompound, i2, SharedConstants.getGameVersion().getSaveVersion().getId());
            return nbttagcompound;
        });
    }*/
    
    /**
     * @reason Spigot Offline UUID
     * @author Cardboard
     */
    /*@Overwrite
    @Nullable
    public Optional<NbtCompound> loadPlayerData_old(PlayerEntity player) {
        NbtCompound lv = null;
        try {
            File file = new File(this.playerDataDir, player.getUuidAsString() + ".dat");
            if (file.exists() && file.isFile()) {
                lv = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
            }
        } catch (Exception exception) {
        	CardboardMod.LOGGER.warning("Failed to load player data for " + player.getName().getString());
        }
        if (lv != null) {
        	// Cardboard Start
        	if (player instanceof ServerPlayerEntity) {
                CraftPlayer craftPlayer = (CraftPlayer) ((IMixinServerEntityPlayer)player).getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDataDir, player.getUuid() + ".dat").lastModified();
                if (modified < craftPlayer.getFirstPlayed()) {
                    craftPlayer.setFirstPlayed(modified);
                }
            }
        	// Cardboard End
            int i = NbtHelper.getDataVersion(lv, -1);
            player.readNbt(DataFixTypes.PLAYER.update(this.dataFixer, lv, i));
        }
        return lv;
    }*/
    
    /**
     * @reason Spigot Offline UUID
     * @author BukkitFabric
     *
    // @Overwrite
    public NbtCompound loadPlayerData_(PlayerEntity entityhuman) {
        NbtCompound nbttagcompound = null;

        try {
            File file = new File(this.playerDataDir, entityhuman.getUuidAsString() + ".dat");
            // Spigot Start
            boolean usingWrongFile = false;
            if (!file.exists()) {
                file = new File( this.playerDataDir, java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + entityhuman.getEntityName()).getBytes(StandardCharsets.UTF_8)).toString() + ".dat");
                if (file.exists()) {
                    usingWrongFile = true;
                    org.bukkit.Bukkit.getServer().getLogger().warning("Using offline mode UUID file for player " + entityhuman.getEntityName() + " as it is the only copy we can find");
                }
            } // Spigot End

            if (file.exists() && file.isFile())
                nbttagcompound = NbtIo.readCompressed(file);

            if (usingWrongFile) // Spigot
                file.renameTo(new File(file.getPath() + ".offline-read")); // Spigot
        } catch (Exception exception) {
            CardboardMod.LOGGER.warning("Failed to load player data for " + entityhuman.getName().getString());
        }

        if (nbttagcompound != null) {
            // CraftBukkit start
            if (entityhuman instanceof ServerPlayerEntity) {
                CraftPlayer player = (CraftPlayer) ((IMixinServerEntityPlayer)entityhuman).getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDataDir, entityhuman.getUuid().toString() + ".dat").lastModified();
                if (modified < player.getFirstPlayed()) {
                    player.setFirstPlayed(modified);
                }
            }
            // CraftBukkit end
            int i = nbttagcompound.contains("DataVersion", 3) ? nbttagcompound.getInt("DataVersion") : -1;

            entityhuman.readNbt(NbtHelper.update(this.dataFixer, DataFixTypes.PLAYER, nbttagcompound, i));
        }

        return nbttagcompound;
    }*/

    @SuppressWarnings("resource")
    @Override
    public CompoundTag getPlayerData(String s) {
        try {
            File file1 = new File(this.playerDir, s + ".dat");
            if (file1.exists()) {
                return NbtIo.readCompressed(new FileInputStream(file1), NbtAccounter.unlimitedHeap());
            }
        } catch (Exception exception) {
            CardboardMod.LOGGER.warning("Failed to load player data for " + s);
        }

        return null;
    }

    // CraftBukkit start
    @Override
    public File cardboard$getPlayerDir() {
        return this.playerDir;
    }
    // CraftBukkit end
}
