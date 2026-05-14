package io.papermc.paper.pluginremap;

import com.mojang.logging.LogUtils;
import io.papermc.paper.util.Hashing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
final class UnknownOriginRemappedPluginIndex extends RemappedPluginIndex {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Set<String> used = new HashSet<>();

   UnknownOriginRemappedPluginIndex(Path dir) {
      super(dir, true);
   }

   @Override
   Path getIfPresent(Path in) {
      String hash = Hashing.sha256(in);
      if (this.state.skippedHashes.contains(hash)) {
         return in;
      } else {
         Path path = super.getIfPresent(hash);
         if (path != null) {
            this.used.add(hash);
         }

         return path;
      }
   }

   @Override
   Path input(Path in) {
      String hash = Hashing.sha256(in);
      this.used.add(hash);
      return super.input(in, hash);
   }

   void write(boolean clean) {
      if (!clean) {
         super.write();
      } else {
         Iterator<Entry<String, String>> it = this.state.hashes.entrySet().iterator();

         while (it.hasNext()) {
            Entry<String, String> next = it.next();
            if (!this.used.contains(next.getKey())) {
               it.remove();
               Path file = this.dir().resolve(next.getValue());

               try {
                  Files.deleteIfExists(file);
               } catch (IOException var6) {
                  LOGGER.warn("Failed to delete no longer needed cached jar '{}'", file, var6);
               }
            }
         }

         super.write();
      }
   }
}
