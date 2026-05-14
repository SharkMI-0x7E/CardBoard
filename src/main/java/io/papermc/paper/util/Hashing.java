package io.papermc.paper.util;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class Hashing {
   private Hashing() {
   }

   public static String sha256(InputStream stream) {
      try {
         InputStream ex = stream;

         String var2;
         try {
            var2 = com.google.common.hash.Hashing.sha256().hashBytes(IOUtils.toByteArray(stream)).toString().toUpperCase(Locale.ROOT);
         } catch (Throwable var5) {
            if (stream != null) {
               try {
                  ex.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (stream != null) {
            stream.close();
         }

         return var2;
      } catch (IOException var6) {
         throw new RuntimeException("Failed to take hash of InputStream", var6);
      }
   }

   public static String sha256(Path file) {
      if (!Files.isRegularFile(file)) {
         throw new IllegalArgumentException("'" + file + "' is not a regular file!");
      } else {
         HashCode hash;
         try {
            hash = com.google.common.io.Files.asByteSource(file.toFile()).hash(com.google.common.hash.Hashing.sha256());
         } catch (IOException var3) {
            throw new RuntimeException("Failed to take hash of file '" + file + "'", var3);
         }

         return hash.toString().toUpperCase(Locale.ROOT);
      }
   }
}
