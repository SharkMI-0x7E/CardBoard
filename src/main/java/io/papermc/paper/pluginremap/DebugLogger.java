package io.papermc.paper.pluginremap;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
final class DebugLogger implements Consumer<String>, AutoCloseable {
   @Nullable
   private final PrintWriter writer;

   DebugLogger(Path logFile) {
      try {
         this.writer = createWriter(logFile);
      } catch (IOException var3) {
         throw new RuntimeException("Failed to initialize DebugLogger for file '" + logFile + "'", var3);
      }
   }

   public void accept(String line) {
      this.useWriter(writer -> writer.println(line));
   }

   @Override
   public void close() {
      this.useWriter(PrintWriter::close);
   }

   private void useWriter(Consumer<PrintWriter> op) {
      PrintWriter writer = this.writer;
      if (writer != null) {
         op.accept(writer);
      }
   }

   Consumer<String> debug() {
      return line -> this.accept("[debug]: " + line);
   }

   static DebugLogger forOutputFile(Path outputFile) {
      return new DebugLogger(outputFile.resolveSibling(outputFile.getFileName() + ".log"));
   }

   @Nullable
   private static PrintWriter createWriter(Path logFile) throws IOException {
      if (!PluginRemapper.DEBUG_LOGGING) {
         return null;
      } else {
         if (!Files.exists(logFile.getParent())) {
            Files.createDirectories(logFile.getParent());
         }

         return new PrintWriter(logFile.toFile());
      }
   }
}
