package io.papermc.paper.pluginremap.reflect;

import io.papermc.asm.ClassInfoProvider;
import io.papermc.asm.RewriteRuleVisitorFactory;
import io.papermc.paper.util.MappingEnvironment;
import io.papermc.reflectionrewriter.BaseReflectionRules;
import io.papermc.reflectionrewriter.DefineClassRule;
import io.papermc.reflectionrewriter.proxygenerator.ProxyGenerator;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

@DefaultQualifier(NonNull.class)
public final class ReflectionRemapper {
   private static final String PAPER_REFLECTION_HOLDER = "io.papermc.paper.pluginremap.reflect.PaperReflectionHolder";
   private static final String PAPER_REFLECTION_HOLDER_DESC = "io.papermc.paper.pluginremap.reflect.PaperReflectionHolder".replace('.', '/');
   private static final RewriteRuleVisitorFactory VISITOR_FACTORY = RewriteRuleVisitorFactory.create(
      589824,
      chain -> chain.then(new BaseReflectionRules("io.papermc.paper.pluginremap.reflect.PaperReflectionHolder").rules())
         .then(DefineClassRule.create(PAPER_REFLECTION_HOLDER_DESC, true)),
      ClassInfoProvider.basic()
   );

   private ReflectionRemapper() {
   }

   public static ClassVisitor visitor(ClassVisitor parent) {
      return !MappingEnvironment.reobf() && !MappingEnvironment.DISABLE_PLUGIN_REMAPPING ? VISITOR_FACTORY.createVisitor(parent) : parent;
   }

   public static byte[] processClass(byte[] bytes) {
      if (MappingEnvironment.DISABLE_PLUGIN_REMAPPING) {
         return bytes;
      } else {
         ClassReader classReader = new ClassReader(bytes);
         ClassWriter classWriter = new ClassWriter(classReader, 0);
         classReader.accept(visitor(classWriter), 0);
         return classWriter.toByteArray();
      }
   }

   private static void setupProxy() {
      try {
         byte[] bytes = ProxyGenerator.generateProxy(PaperReflection.class, PAPER_REFLECTION_HOLDER_DESC);
         Lookup lookup = MethodHandles.lookup();
         Class<?> generated = lookup.defineClass(bytes);
         Method init = generated.getDeclaredMethod("init", PaperReflection.class);
         init.invoke(null, new PaperReflection());
      } catch (ReflectiveOperationException var4) {
         throw new RuntimeException(var4);
      }
   }

   static {
      if (!MappingEnvironment.reobf()) {
         setupProxy();
      }
   }
}
