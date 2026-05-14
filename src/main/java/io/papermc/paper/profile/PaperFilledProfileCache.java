package io.papermc.paper.profile;

import com.mojang.authlib.GameProfile;

import java.lang.StackWalker.Option;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.server.MinecraftServer;

import org.bukkit.craftbukkit.CraftServer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spigotmc.SpigotConfig;

@NullMarked
public final class PaperFilledProfileCache {

	private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
	public static Logger LogUtils_getClassLogger() { return LoggerFactory.getLogger(STACK_WALKER.getCallerClass().getSimpleName()); }

   private static final Logger LOGGER = LogUtils_getClassLogger();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private final Map<String, GameProfile> byName = new HashMap<>();
   private final Map<UUID, GameProfile> byId = new HashMap<>();
   private final Map<UUID, Long> lastOperation = new ConcurrentHashMap<>();
   private final AtomicLong operationCount = new AtomicLong(0L);
   private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(
      Thread.ofPlatform()
         .name(this.getClass().getSimpleName() + "-cleanup-", 0L)
         .daemon(true)
         .uncaughtExceptionHandler((thread, throwable) -> LOGGER.warn("Uncaught exception in thread {}", thread.getName(), throwable))
         .factory()
   );

   public PaperFilledProfileCache() {
      this.cleanupExecutor.scheduleAtFixedRate(() -> {
         if (!this.tryCancelCleanup()) {
            this.performCleanup();
         }
      }, 15L, 15L, TimeUnit.SECONDS);
   }

   public void updateIfCached(GameProfile profile) {
      if (this.getIfCached(profile.id()) != null) {
         this.add(profile);
      }
   }

   public void add(GameProfile profile) {
      try {
         this.lock.writeLock().lock();
         this.byName.put(profile.name(), profile);
         this.byId.put(profile.id(), profile);
         this.lastOperation.put(profile.id(), this.operationCount.getAndIncrement());
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   @Nullable
   public GameProfile getIfCached(UUID uuid) {
      GameProfile var3;
      try {
         this.lock.readLock().lock();
         GameProfile profile = this.byId.get(uuid);
         if (profile != null) {
            this.lastOperation.put(uuid, this.operationCount.getAndIncrement());
         }

         var3 = profile;
      } finally {
         this.lock.readLock().unlock();
      }

      return var3;
   }

   @Nullable
   public GameProfile getIfCached(String name) {
      GameProfile var3;
      try {
         this.lock.readLock().lock();
         GameProfile profile = this.byName.get(name);
         if (profile != null) {
            this.lastOperation.put(profile.id(), this.operationCount.getAndIncrement());
         }

         var3 = profile;
      } finally {
         this.lock.readLock().unlock();
      }

      return var3;
   }

   private int maxSize() {
      return SpigotConfig.userCacheCap;
   }

   private boolean tryCancelCleanup() {
      MinecraftServer server = CraftServer.server;
      
      // todo: use spigot hasStopped instead of MC isStopped
      
      if (server == null || server.isRunning() && !server.isStopped()) {
         return false;
      } else {
         this.cleanupExecutor.shutdown();
         return true;
      }
   }

   private void performCleanup() {
      int maxSize = this.maxSize();
      if (this.lastOperation.size() > maxSize) {
         try {
            this.lock.writeLock().lock();
            List<UUID> list = this.lastOperation.entrySet().stream().sorted(Entry.comparingByValue()).map(Entry::getKey).toList();
            Iterator<UUID> iterator = list.iterator();

            while (iterator.hasNext() && this.lastOperation.size() > maxSize) {
               UUID uuid = iterator.next();
               this.lastOperation.remove(uuid);
               GameProfile profile = this.byId.remove(uuid);
               this.byName.remove(profile.name());
            }
         } finally {
            this.lock.writeLock().unlock();
         }
      }
   }
}
