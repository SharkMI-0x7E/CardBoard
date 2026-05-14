package io.papermc.paper.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class StringPool {
   private final Map<String, String> pool;

   public StringPool() {
      this(new HashMap<>());
   }

   public StringPool(Map<String, String> map) {
      this.pool = map;
   }

   public String string(String string) {
      return this.pool.computeIfAbsent(string, Function.identity());
   }
}
