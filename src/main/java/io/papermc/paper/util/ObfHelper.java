package io.papermc.paper.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.neoforged.srgutils.IMappingFile;
import net.neoforged.srgutils.IMappingFile.IClass;
import net.neoforged.srgutils.IMappingFile.IField;
import net.neoforged.srgutils.IMappingFile.IMethod;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public enum ObfHelper {
   INSTANCE;

   @Nullable
   private final Map<String, ObfHelper.ClassMapping> mappingsByObfName;
   @Nullable
   private final Map<String, ObfHelper.ClassMapping> mappingsByMojangName;

   private ObfHelper() {
      Set<ObfHelper.ClassMapping> maps = loadMappingsIfPresent();
      if (maps != null) {
         this.mappingsByObfName = maps.stream().collect(Collectors.toUnmodifiableMap(ObfHelper.ClassMapping::obfName, map -> (ObfHelper.ClassMapping)map));
         this.mappingsByMojangName = maps.stream()
            .collect(Collectors.toUnmodifiableMap(ObfHelper.ClassMapping::mojangName, map -> (ObfHelper.ClassMapping)map));
      } else {
         this.mappingsByObfName = null;
         this.mappingsByMojangName = null;
      }
   }

   @Nullable
   public Map<String, ObfHelper.ClassMapping> mappingsByObfName() {
      return this.mappingsByObfName;
   }

   @Nullable
   public Map<String, ObfHelper.ClassMapping> mappingsByMojangName() {
      return this.mappingsByMojangName;
   }

   public String reobfClassName(String fullyQualifiedMojangName) {
      if (this.mappingsByMojangName == null) {
         return fullyQualifiedMojangName;
      } else {
         ObfHelper.ClassMapping map = this.mappingsByMojangName.get(fullyQualifiedMojangName);
         return map == null ? fullyQualifiedMojangName : map.obfName();
      }
   }

   public String deobfClassName(String fullyQualifiedObfName) {
      if (this.mappingsByObfName == null) {
         return fullyQualifiedObfName;
      } else {
         ObfHelper.ClassMapping map = this.mappingsByObfName.get(fullyQualifiedObfName);
         return map == null ? fullyQualifiedObfName : map.mojangName();
      }
   }

   @Nullable
   private static Set<ObfHelper.ClassMapping> loadMappingsIfPresent() {
      if (!MappingEnvironment.hasMappings()) {
         return null;
      } else {
         try {
            Set var14;
            try (InputStream mappingsInputStream = MappingEnvironment.mappingsStream()) {
               IMappingFile mappings = IMappingFile.load(mappingsInputStream);
               Set<ObfHelper.ClassMapping> classes = new HashSet<>();
               StringPool pool = new StringPool();

               for (IClass cls : mappings.getClasses()) {
                  Map<String, String> methods = new HashMap<>();
                  Map<String, String> fields = new HashMap<>();
                  Map<String, String> strippedMethods = new HashMap<>();

                  for (IMethod methodMapping : cls.getMethods()) {
                     methods.put(
                        pool.string(methodKey(Objects.requireNonNull(methodMapping.getMapped()), Objects.requireNonNull(methodMapping.getMappedDescriptor()))),
                        pool.string(Objects.requireNonNull(methodMapping.getOriginal()))
                     );
                     strippedMethods.put(
                        pool.string(pool.string(strippedMethodKey(methodMapping.getMapped(), methodMapping.getDescriptor()))),
                        pool.string(methodMapping.getOriginal())
                     );
                  }

                  for (IField field : cls.getFields()) {
                     fields.put(pool.string(field.getMapped()), pool.string(field.getOriginal()));
                  }

                  ObfHelper.ClassMapping map = new ObfHelper.ClassMapping(
                     Objects.requireNonNull(cls.getMapped()).replace('/', '.'),
                     Objects.requireNonNull(cls.getOriginal()).replace('/', '.'),
                     Map.copyOf(methods),
                     Map.copyOf(fields),
                     Map.copyOf(strippedMethods)
                  );
                  classes.add(map);
               }

               var14 = Set.copyOf(classes);
            }

            return var14;
         } catch (IOException var13) {
            System.err.println("Failed to load mappings.");
            var13.printStackTrace();
            return null;
         }
      }
   }

   public static String strippedMethodKey(String methodName, String methodDescriptor) {
      String methodKey = methodKey(methodName, methodDescriptor);
      int returnDescriptorEnd = methodKey.indexOf(41);
      return methodKey.substring(0, returnDescriptorEnd + 1);
   }

   public static String methodKey(String methodName, String methodDescriptor) {
      return methodName + methodDescriptor;
   }

   public record ClassMapping(
      String obfName, String mojangName, Map<String, String> methodsByObf, Map<String, String> fieldsByObf, Map<String, String> strippedMethods
   ) {
   }
}
