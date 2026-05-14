package io.papermc.paper.pluginremap.reflect;

import com.mojang.logging.LogUtils;
import io.papermc.paper.util.MappingEnvironment;
import io.papermc.paper.util.ObfHelper;
import io.papermc.reflectionrewriter.runtime.AbstractDefaultRulesReflectionProxy;
import io.papermc.reflectionrewriter.runtime.DefineClassReflectionProxy;
import java.lang.invoke.MethodHandles.Lookup;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

@DefaultQualifier(NonNull.class)
public final class PaperReflection extends AbstractDefaultRulesReflectionProxy implements DefineClassReflectionProxy {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String CB_PACKAGE_PREFIX = "org.bukkit.".concat("craftbukkit.");
   private static final String LEGACY_CB_PACKAGE_PREFIX = "org.bukkit.".concat("craftbukkit.") + "v1_21_R7.";
   private final DefineClassReflectionProxy defineClassProxy = DefineClassReflectionProxy.create(PaperReflection::processClass);
   private final Map<String, ObfHelper.ClassMapping> mappingsByMojangName;
   private final Map<String, ObfHelper.ClassMapping> mappingsByObfName;
   private final Map<String, Map<String, String>> strippedMethodMappings;

   PaperReflection() {
      if (!MappingEnvironment.hasMappings()) {
         this.mappingsByMojangName = Map.of();
         this.mappingsByObfName = Map.of();
         this.strippedMethodMappings = Map.of();
      } else {
         ObfHelper obfHelper = ObfHelper.INSTANCE;
         this.mappingsByMojangName = Objects.requireNonNull(obfHelper.mappingsByMojangName(), "mappingsByMojangName");
         this.mappingsByObfName = Objects.requireNonNull(obfHelper.mappingsByObfName(), "mappingsByObfName");
         this.strippedMethodMappings = this.mappingsByMojangName
            .entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, entry -> entry.getValue().strippedMethods()));
      }
   }

   protected String mapClassName(String name) {
      ObfHelper.ClassMapping mapping = this.mappingsByObfName.get(name);
      return mapping != null ? mapping.mojangName() : removeCraftBukkitRelocation(name);
   }

   protected String mapDeclaredMethodName(Class<?> clazz, String name, Class<?>... parameterTypes) {
      Map<String, String> mapping = this.strippedMethodMappings.get(clazz.getName());
      return mapping == null ? name : mapping.getOrDefault(strippedMethodKey(name, parameterTypes), name);
   }

   protected String mapMethodName(Class<?> clazz, String name, Class<?>... parameterTypes) {
      String mapped = this.findMappedMethodName(clazz, name, parameterTypes);
      return mapped != null ? mapped : name;
   }

   protected String mapDeclaredFieldName(Class<?> clazz, String name) {
      ObfHelper.ClassMapping mapping = this.mappingsByMojangName.get(clazz.getName());
      return mapping == null ? name : mapping.fieldsByObf().getOrDefault(name, name);
   }

   protected String mapFieldName(Class<?> clazz, String name) {
      String mapped = this.findMappedFieldName(clazz, name);
      return mapped != null ? mapped : name;
   }

   private String findMappedMethodName(Class<?> clazz, String name, Class<?>... parameterTypes) {
      Map<String, String> map = this.strippedMethodMappings.get(clazz.getName());
      String mapped = null;
      if (map != null) {
         mapped = map.get(strippedMethodKey(name, parameterTypes));
         if (mapped != null) {
            return mapped;
         }
      }

      Class<?> superClass = clazz.getSuperclass();
      if (superClass != null) {
         mapped = this.findMappedMethodName(superClass, name, parameterTypes);
      }

      if (mapped == null) {
         for (Class<?> i : clazz.getInterfaces()) {
            mapped = this.findMappedMethodName(i, name, parameterTypes);
            if (mapped != null) {
               break;
            }
         }
      }

      return mapped;
   }

   private String findMappedFieldName(Class<?> clazz, String name) {
      ObfHelper.ClassMapping mapping = this.mappingsByMojangName.get(clazz.getName());
      String mapped = null;
      if (mapping != null) {
         mapped = mapping.fieldsByObf().get(name);
         if (mapped != null) {
            return mapped;
         }
      }

      Class<?> superClass = clazz.getSuperclass();
      if (superClass != null) {
         mapped = this.findMappedFieldName(superClass, name);
      }

      if (mapped == null) {
         for (Class<?> i : clazz.getInterfaces()) {
            mapped = this.findMappedFieldName(i, name);
            if (mapped != null) {
               break;
            }
         }
      }

      return mapped;
   }

   private static String strippedMethodKey(String methodName, @Nullable Class<?>... parameterTypes) {
      return methodName + parameterDescriptor(parameterTypes);
   }

   private static String parameterDescriptor(@Nullable Class<?>... parameterTypes) {
      if (parameterTypes == null) {
         return "()";
      } else {
         StringBuilder builder = new StringBuilder();
         builder.append('(');

         for (Class<?> parameterType : parameterTypes) {
            builder.append(parameterType.descriptorString());
         }

         builder.append(')');
         return builder.toString();
      }
   }

   private static String removeCraftBukkitRelocation(String name) {
      if (MappingEnvironment.hasMappings()) {
         return name;
      } else {
         return name.startsWith(LEGACY_CB_PACKAGE_PREFIX) ? CB_PACKAGE_PREFIX + name.substring(LEGACY_CB_PACKAGE_PREFIX.length()) : name;
      }
   }

   public Class<?> defineClass(Object loader, byte[] b, int off, int len) throws ClassFormatError {
      return this.defineClassProxy.defineClass(loader, b, off, len);
   }

   public Class<?> defineClass(Object loader, String name, byte[] b, int off, int len) throws ClassFormatError {
      return this.defineClassProxy.defineClass(loader, name, b, off, len);
   }

   public Class<?> defineClass(Object loader, @Nullable String name, byte[] b, int off, int len, @Nullable ProtectionDomain protectionDomain) throws ClassFormatError {
      return this.defineClassProxy.defineClass(loader, name, b, off, len, protectionDomain);
   }

   public Class<?> defineClass(Object loader, String name, ByteBuffer b, ProtectionDomain protectionDomain) throws ClassFormatError {
      return this.defineClassProxy.defineClass(loader, name, b, protectionDomain);
   }

   public Class<?> defineClass(Object secureLoader, String name, byte[] b, int off, int len, CodeSource cs) {
      return this.defineClassProxy.defineClass(secureLoader, name, b, off, len, cs);
   }

   public Class<?> defineClass(Object secureLoader, String name, ByteBuffer b, CodeSource cs) {
      return this.defineClassProxy.defineClass(secureLoader, name, b, cs);
   }

   public Class<?> defineClass(Lookup lookup, byte[] bytes) throws IllegalAccessException {
      return this.defineClassProxy.defineClass(lookup, bytes);
   }

   private static byte[] processClass(byte[] bytes) {
      try {
         return ReflectionRemapper.processClass(bytes);
      } catch (Exception var2) {
         LOGGER.warn("Failed to process class bytes", var2);
         return bytes;
      }
   }
}
