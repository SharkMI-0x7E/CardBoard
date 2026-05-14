package org.cardboardpowered.util.nms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.CardboardLogger;
import org.cardboardpowered.mappings.TsrgWriter;
import org.cardboardpowered.mohistremap.ClassMapping;
import org.cardboardpowered.mohistremap.IRemapUtils;
import org.cardboardpowered.mohistremap.utils.ASMUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mohistmc.remap.remappers.BannerInheritanceMap;
import com.mohistmc.remap.remappers.BannerInheritanceProvider;
import com.mohistmc.remap.remappers.BannerJarMapping;
import com.mohistmc.remap.remappers.BannerJarRemapper;
import com.mohistmc.remap.remappers.BannerSuperClassRemapper;
import com.mohistmc.remap.remappers.ClassRemapperSupplier;
import com.mohistmc.remap.remappers.ReflectMethodRemapper;
import com.mohistmc.remap.remappers.ReflectRemapper;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JointProvider;
import net.minecraft.core.Registry;

/**
 * @author pyz
 * @date 2019/6/30 11:50 PM
 */
public class RemapUtils implements IRemapUtils {
	
	private static CardboardLogger LOGGER = CardboardLogger.get("RemapUtils");

    public static BannerJarMapping jarMapping;
    public static BannerJarRemapper jarRemapper;
    private static final List<Remapper> remappers = new ArrayList<>();
    public static InheritanceMap inheritanceMap;
    
    public static boolean DEBUG_VERBOSE_CALLS = true;

    public static String NMS_VERSION = "v1_21_R7"; // "v1_21_R3"; // "v1_20_R4";
    
    private static MyMappingResolver myMappingResolver;
    
    private static HashMap<String, String> cspigot2fabric = new HashMap<>();
    
    public static File exportResource(String res, File folder) {
        try (InputStream stream = RemapUtils.class.getClassLoader().getResourceAsStream("mappings/" + res)) {
            if (stream == null) throw new IOException("Null " + res);

            Path p = Paths.get(folder.getAbsolutePath() + File.separator + res);
            Files.copy(stream, p, StandardCopyOption.REPLACE_EXISTING);
            return p.toFile();
        } catch (IOException e) { e.printStackTrace(); return null;}
    }
    
    public static File exportResourceMeta(String res, File folder) {
        try (InputStream stream = RemapUtils.class.getClassLoader().getResourceAsStream("META-INF/mappings/" + res)) {
            if (stream == null) throw new IOException("Null " + res);

            Path p = Paths.get(folder.getAbsolutePath() + File.separator + res);
            Files.copy(stream, p, StandardCopyOption.REPLACE_EXISTING);
            return p.toFile();
        } catch (IOException e) { e.printStackTrace(); return null;}
    }

	@Override
    public void init() {
    	LOGGER.debug("Remap Util init");

    	inheritanceMap = new CbInheritanceMap();
    	
        jarMapping = new BannerJarMapping();
        // v1_20_R1
        jarMapping.packages.put("org/bukkit/craftbukkit/" + NMS_VERSION + "/", "org/bukkit/craftbukkit/");
        jarMapping.packages.put("org/bukkit/craftbukkit/" + NMS_VERSION, "org/bukkit/craftbukkit");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/it/unimi/dsi/fastutil/", "it/unimi/dsi/fastutil/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/jline/", "jline/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/org/apache/commons/", "org/apache/commons/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/org/objectweb/asm/", "org/objectweb/asm/");
        
        
        //jarMapping.classes.put("org/bukkit/craftbukkit/" + NMS_VERSION + "/CraftServer", "org/bukkit/craftbukkit/CraftServer");
        //jarMapping.classes.put("org/bukkit/craftbukkit/" + NMS_VERSION + "/CraftWorld", "org/cardboardpowered/impl/world/CraftWorld");
        
        jarMapping.classes.put("org/spigotmc/event/entity/EntityMountEvent", "org/bukkit/event/entity/EntityMountEvent");
        jarMapping.classes.put("org/spigotmc/event/entity/EntityDismountEvent", "org/bukkit/event/entity/EntityDismountEvent");
        
    	// jarMapping.classes.put("net/ess3/provider/providers/LegacyPotionMetaProvider", "net/ess3/provider/providers/ModernPotionMetaProvider");

        
        jarMapping.setInheritanceMap(new BannerInheritanceMap());
        jarMapping.setFallbackInheritanceProvider(new BannerInheritanceProvider());

        MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 

        try {
            jarMapping.loadMappings(
                    new BufferedReader(new InputStreamReader(RemapUtils.class.getClassLoader()
                            // .getResourceAsStream("mappings/spigot2srg-1.20.srg"))),
                    		.getResourceAsStream("mappings/testsrg.srg"))),
                    null,
                    null, false);
        } catch (Exception e) {
        	System.out.println("debug: error loading remaputils");
            e.printStackTrace();
        }

        File dir = new File("mappings");
        dir.mkdirs();

        BiMap<String, String> inverseClassMap = HashBiMap.create(jarMapping.classes).inverse();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(RemapUtils.class.getClassLoader().getResourceAsStream("mappings/inheritanceMap.txt")))) {
            inheritanceMap.load(reader, inverseClassMap);
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        try {
        	LOGGER.info("Exporting Mojang Mappings...");
        	Path path = exportResourceMeta("reobf.tiny", dir).toPath();
			myMappingResolver = new MyMappingResolver(path, MappingFormat.TINY_2_FILE, "named", "intermediary");
			LOGGER.info("RemapUtils: MyMappingResolver Namespace: " + myMappingResolver.getCurrentRuntimeNamespace());
			
			output(path);
			
			myMappingResolver.tree.getClasses().forEach(clm -> {
				String a = clm.getName("named");
				String b = clm.getName("intermediary").replace('.', '/');

					jarMapping.classes.put(a, b);
					jarMapping.registerClassMapping(a, b);
				//}
			});
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        String bukkit_cl_srg = "bukkit-1.21.11-cl.csrg";
        File bukkit_class_mappings_file = exportResource(bukkit_cl_srg, dir);
        
        jarMapping.classes.put("org/bukkit/craftbukkit/" + NMS_VERSION + "/CraftWorld", "org/cardboardpowered/impl/world/CraftWorld");
        
        // some missing
        jarMapping.classes.put("net/minecraft/nbt/ListTag", "net/minecraft/class_2499");
        jarMapping.classes.put("net/minecraft/class_7225$a", "net/minecraft/class_7225$class_7874");
        
        // Mojang: HolderLookup$Provider
        jarMapping.classes.put("net/minecraft/class_7225$Provider", "net/minecraft/class_7225$class_7874");

        jarMapping.classes.put("net/minecraft/server/dedicated/DedicatedServer", "net/minecraft/class_3176");
        
        jarMapping.registerClassMapping("net/minecraft/class_7225$Provider", "net/minecraft/class_7225$class_7874");
        
        // HashMap<String, String> cm = new HashMap<>();

        String namespace = mr.getCurrentRuntimeNamespace();
        
        addMethodOverrides();

        try {
        	LOGGER.debug("Reading \"" + bukkit_cl_srg + "\".");

        	List<String> lines = Files.readAllLines(bukkit_class_mappings_file.toPath());
        	int count = 0, total = 0;
        	for (String line : lines) {
        		if (line.startsWith("# ")) {
        			continue;
        		}

        		String[] spl = line.split(" ");
        		
        		// Get class names
        		String obf_claz = spl[0];
        		String spi_claz = spl[1];
        		String fab_claz = mr.mapClassName("official", obf_claz.replace('/', '.')).replace('.', '/');

        		if (!fab_claz.equals(obf_claz)) {
        			cspigot2fabric.put(spi_claz, fab_claz);
        			cspigot2fabric.put(spi_claz.replace('/', '.'), fab_claz.replace('/', '.'));
        		}

        		if (!jarMapping.classes.containsKey(spi_claz)) {
        			jarMapping.registerClassMapping(spi_claz, fab_claz);
        			count += 1;
        		} else {
        			String key = jarMapping.classes.get(spi_claz);
        			
        			boolean replace = true;
        			
        			if (namespace.equalsIgnoreCase("named") || replace) {
        				jarMapping.byNMSSrcName.remove(spi_claz);
        				ClassMapping cam = jarMapping.registerClassMapping(spi_claz, fab_claz);

        				cam.setNmsSrcName(spi_claz);
        				cam.setMcpSrcName(fab_claz);
        				count += 1;
        			}
        		}
        		total += 1;
        	}
        	LOGGER.info("Read " + count + " classes from " + bukkit_cl_srg);
        } catch (IOException e) {
        	e.printStackTrace();
        }

        JointProvider provider = new JointProvider();
        provider.add(inheritanceMap);
        provider.add(new ClassLoaderProvider(ClassLoader.getSystemClassLoader()));
        provider.add(new BannerInheritanceProvider());
        //jarMapping.setInheritanceMap(new InheritanceMap());
        jarMapping.setFallbackInheritanceProvider(provider);
        jarRemapper = new BannerJarRemapper(jarMapping);
        remappers.add(jarRemapper);
        remappers.add(new ReflectRemapper());
        jarMapping.initFastMethodMapping(jarRemapper);
        ReflectMethodRemapper.init();

        try {
            Class.forName("org.cardboardpowered.mohistremap.proxy.ProxyMethodHandlesLookup");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        DEBUG_VERBOSE_CALLS = CardboardConfig.DEBUG_VERBOSE_CALLS;
    }
    
    private static void output(Path path) throws IOException {
    	if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
    		return;
    	}
    	
    	 MemoryMappingTree tree = new MemoryMappingTree();
         Tiny2FileReader.read(Files.newBufferedReader(path), tree);

         try (BufferedWriter out = Files.newBufferedWriter(Paths.get("output1.tsrg"))) {
             TsrgWriter.write(tree, out, "official", "named");
         }

         try (BufferedWriter out = Files.newBufferedWriter(Paths.get("output2.tsrg"))) {
             TsrgWriter.write(tree, out, "named", "intermediary");
         }
         
         try (BufferedWriter out = Files.newBufferedWriter(Paths.get("output3.tsrg"))) {
             TsrgWriter.write(tree, out, "official", "intermediary");
         }
         
         try (BufferedWriter out = Files.newBufferedWriter(Paths.get("output4.tsrg"))) {
             TsrgWriter.write(tree, out, "intermediary", "named");
         }
    }

    @Override
    public byte[] remapFindClass(byte[] bs) {
        ClassReader reader = new ClassReader(bs); // Turn from bytes into visitor
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        for (Remapper remapper : remappers) {

            ClassNode container = new ClassNode();
            ClassRemapper classRemapper;
            if (remapper instanceof ClassRemapperSupplier) {
                classRemapper = ((ClassRemapperSupplier) remapper).getClassRemapper(container);
            } else {
                classRemapper = new ClassRemapper(container, remapper);
            }
            classNode.accept(classRemapper);
            classNode = container;
        }
        BannerSuperClassRemapper.init(classNode);
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();

    }

    public static MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 
    
    @Override
    public String map(String typeName) {
    	
    	// Check if typeName is not in internal class name format
    	boolean isRequestNotInternalName = typeName.indexOf('.') != -1 && typeName.indexOf('/') == -1;

        typeName = mapPackage(typeName);
        String res = jarMapping.classes.getOrDefault(typeName, typeName);
        
        if (cspigot2fabric.containsKey(typeName)) {
        	String csf = cspigot2fabric.get(typeName);
        	if (csf.contains("class_")) {
        		typeName = csf;
        	}
        }
        
        // Our Mapping Resolver
    	if (res.equalsIgnoreCase(typeName)) {
    		String res2 = myMappingResolver.mapClassName("named", res.replace('/', '.')).replace('.', '/');
    		if (res2.contains("class_")) {
    			res = res2;
    		}
    	}
        
        //if (res.contains("class_7225")) {
        	// System.out.println("MIS: " + typeName + " / " + res);
        //}
        
        if (typeName.contains("$") && (typeName.contains("net") && typeName.contains("minecraft"))) {
        	
        	
        	if (!typeName.equalsIgnoreCase(res)) {
        		return res;
        	}
        	
        	String[] spl = typeName.split(Pattern.quote("$"));
        	String nam1 = spl[0];
        	String res1 = jarMapping.classes.getOrDefault(nam1, nam1);
        	
        	// Our Mapping Resolver
        	if (res1.equalsIgnoreCase(nam1)) {
        		String res2 = myMappingResolver.mapClassName("named", res1.replace('/', '.')).replace('.', '/');
        		if (res2.contains("class_")) {
        			res1 = res2;
        		}
        	}
        	
        	if (spl[1].contains("class_")) {
        		return res;
        	}
        	
        	String unnam = mr.unmapClassName("official", res1.replace('/', '.'));
        	String namm = mr.mapClassName("official", (unnam + "$" + spl[1]).replace('/', '.'));
        	
        	if (res.indexOf('/') != -1) {
        		namm = namm.replace('.', '/');
        	}
        	
        	if (isRequestNotInternalName) {
        		namm = namm.replace('/', '.');
        	}
        	
        	// System.out.println("$ NAME: " + typeName + " = " + res + " = " + namm);
        	return namm;
        }
        
        /*
        if (typeName.contains("net") && typeName.contains("minecraft")) {
        	if (!res.contains("class_")) {
        		// System.out.println("RemapUtils.map: NAME: " + typeName + " = " + res);
        	}
        }
        */
        
        if (isRequestNotInternalName) {
        	res = res.replace('/', '.');
    	}
        
        return res; // jarMapping.classes.getOrDefault(typeName, typeName);
    }

    @Override
    public String reverseMap(String typeName) {
        ClassMapping mapping = jarMapping.byNMSInternalName.get(typeName);
        return mapping == null ? typeName : mapping.getNmsSrcName();
    }

    @Override
    public String reverseMap(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? ASMUtils.toInternalName(clazz) : mapping.getNmsSrcName();
    }
    
    public String reverseMap_name(String class_name) {
        ClassMapping mapping = jarMapping.byMCPName.get(class_name);
        return mapping == null ? ASMUtils.toInternalName(class_name) : mapping.getNmsSrcName();
    }

    @Override
    public String mapPackage(String typeName) {
        for (Map.Entry<String, String> entry : jarMapping.packages.entrySet()) {
            String prefix = entry.getKey();
            if (typeName.startsWith(prefix)) {
                return entry.getValue() + typeName.substring(prefix.length());
            }
        }
        return typeName;
    }

    @Override
    public String remapMethodDesc(String methodDescriptor) {
        Type rt = Type.getReturnType(methodDescriptor);
        Type[] ts = Type.getArgumentTypes(methodDescriptor);
        rt = Type.getType(ASMUtils.toDescriptorV2(map(ASMUtils.getInternalName(rt))));
        for (int i = 0; i < ts.length; i++) {
            ts[i] = Type.getType(ASMUtils.toDescriptorV2(map(ASMUtils.getInternalName(ts[i]))));
        }
        return Type.getMethodType(rt, ts).getDescriptor();
    }

    @Override
    public String mapMethodName(Class<?> clazz, String name, MethodType methodType) {
        return mapMethodName(clazz, name, methodType.parameterArray());
    }
    
    private static HashMap<String, String> methodExtra = new HashMap<>();
    
    private static void addMethodOverrides() {
    	methodExtra.put("class_2960.fromNamespaceAndPath(java.lang.String, java.lang.String)", "method_60655");
    	methodExtra.put("class_2378.wrapAsHolder(java.lang.Object)", "method_47983");
    	methodExtra.put("class_2629.c(net.minecraft.class_1259)", "method_34096");
    }

    private static String getMethodDescriptor(Class<?>... parameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> param : parameterTypes) {
            sb.append(getDescriptor(param));
        }
        sb.append(')');
        // sb.append('V'); // if you want to include return type, replace 'V' with getDescriptor(returnType)
        return sb.toString();
    }

    private static String getDescriptor(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == void.class) return "V";
            else if (clazz == boolean.class) return "Z";
            else if (clazz == byte.class) return "B";
            else if (clazz == char.class) return "C";
            else if (clazz == short.class) return "S";
            else if (clazz == int.class) return "I";
            else if (clazz == long.class) return "J";
            else if (clazz == float.class) return "F";
            else if (clazz == double.class) return "D";
        } else if (clazz.isArray()) {
            return clazz.getName().replace('.', '/');
        } else {
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
        throw new IllegalArgumentException("Unknown type: " + clazz);
    }

    @Override
    public String mapMethodName(Class<?> type, String name, Class<?>... parameterTypes) {
    	
    	// ReflectionMethodVisitor.do_map(type.getName(), name, parameterTypes);
    	
    	String mm = "";
    	for (Class<?> pt : parameterTypes) {
    		mm += ", " + pt.getName();
    	}
    	mm += ")";
    	
    	if (mm.length() > 2) {
    		mm = mm.substring(2);
    	}
    	mm = "(" + mm;
    	
    	String res = jarMapping.fastMapMethodName(type, name, parameterTypes);

    	//String resv = reverseMap(type);
    	
    	//System.out.println("DEBUG: " + type.getName() + "(" + resv + ")" + " / " + name + " (" + mm + " = " + res);
    	
    	if (res.length() <= 2) {
    		String cl = type.getName();
    		String cll = mr.unmapClassName("official", cl);
    		String match = find_meth(cll, type, name, parameterTypes);

    		if (match.contains("method_")) {
    	    	if (CardboardConfig.DEBUG_LOG_REMAP) {
    	    		LOGGER.info("mapMethodNameA: " + type.getName() + "." + name + " -> " + match + " " + mm);
    	    	}
    			return match;
    		}
    	}

    	if (DEBUG_VERBOSE_CALLS) {
    		System.out.println("Reflection: " + type.getName() + " / " + res);
    		dewe(type, "Debug: METH: " + type.getName() + " / " + name + " = " + res);
    	}

    	if (!res.contains("method_")) {
    		String key = type.getSimpleName() + "." + res + mm;
    		if (methodExtra.containsKey(key)) {
    			res = methodExtra.get(key);
    		}
    		
    		// Check our MappingResolver
            if (name.equalsIgnoreCase(res) && type.getName().contains("class_")) {
            	
            	String desc = getMethodDescriptor(parameterTypes);
            	
            	String clazz = myMappingResolver.unmapClassName("named", type.getName()); // Running -> Mojang
            	String mapp = myMappingResolver.mapMethodNameSafe("named", clazz, res, desc, "intermediary");
            	if (mapp.equalsIgnoreCase(res)) {
            		mapp = myMappingResolver.mapMethodName("named", clazz, res, desc);
            	}
            	
            	// System.out.println("DeBUGff: " + type.getName() + ": " + key + " = " + res + " -> " + mapp);
            	
            	// System.out.println("DeBUGM: " + clazz + ": " + key + " = " + mapp + " " + desc);
            	
            	if (mapp.contains("method_") || mapp.contains("comp_")) {
            		dewe(type, "Debug: METH: " + type.getName() + " / " + name + " = " + mapp);
            		return mapp;
            	} else {
            		// System.out.println("DeBUGM: " + clazz + ": " + key + " = " + mapp + " " + mm);
            		
            		Class<?> superCl = type.getSuperclass();
            		if (null != superCl) {
            			String nam2 = mapMethodName_2(superCl, res, mapp, desc);
            			if (nam2.contains("method_") || nam2.contains("comp_")) {
            				dewe(type, "Debug: METH: " + type.getName() + " / " + name + " = " + nam2);
            				return nam2;
            			}
            		}

            	}
            }
    	}
    	
    	if (CardboardConfig.DEBUG_LOG_REMAP) {
    		// if (!(name.startsWith("method_") && name.equals(res))) {
    		LOGGER.info("mapMethodNameB: " + type.getName() + "." + name + " -> " + res + " " + mm);
    	}
    	
        return res;
    }
    
    public static String mapMethodName_2(Class<?> type, String name, String res, String desc) {
    	String clazz = myMappingResolver.unmapClassName("named", type.getName()); // Running -> Mojang
    	String mapp = myMappingResolver.mapMethodNameSafe("named", clazz, res, desc, "intermediary");
    	if (mapp.equalsIgnoreCase(res)) {
    		mapp = myMappingResolver.mapMethodName("named", clazz, res, desc);
    	}
    	
    	if (mapp.contains("method_") || mapp.contains("comp_")) {
    		return mapp;
    	}
    	return res;
    }

    
    /**
     * Find matching declared method of a class.
     */
    public String find_meth(String obfClaz, Class<?> type, String name, Class<?>... parameterTypes) {
    	System.out.println("FINDING MATCH FOR: " + obfClaz + "." + name);
    	for (Method m : type.getDeclaredMethods()) {
    		int pc = m.getParameterCount();
    		
    		if (parameterTypes.length != pc) {
    			continue;
    		}

    		try {
	    		String desc = getMethodDescriptor(m, "official");
	    		String mmm = mr.mapMethodName("official", obfClaz, name, desc);
	    		// System.out.println("POSS: " + obfClaz + "." + mmm);

	    		if (mmm.contains("method_")) {
	    			// Found Match
	    			return mmm;
	    		}
    		} catch (IllegalArgumentException e) {
    			// noop
    		}
    		
    	}
    	return name;
    }

    @Override
    public String inverseMapMethodName(Class<?> type, String name, Class<?>... parameterTypes) {
        return jarMapping.fastReverseMapMethodName(type, name, parameterTypes);
    }

    @Override
    public String mapFieldName(Class<?> type, String fieldName) {
    	if (DEBUG_VERBOSE_CALLS) {
    		System.out.println("Reflection: " + type.getName() + " / " + fieldName);
    	}
    	
    	String revType = reverseMap(type);
        String key = revType + "/" + fieldName;

        String mapped = jarMapping.fields.get(key);
        if (mapped == null) {
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                mapped = mapFieldName(superClass, fieldName);
            }
        }
        
        String res = mapped != null ? mapped : fieldName;

        if (DEBUG_VERBOSE_CALLS) {
        	System.out.println("DEBUG: FIELD: " + revType + "/" + type.getName() + " / " + fieldName + " = " + (mapped != null ? mapped : fieldName));
        }
        
        // dewe(type, "Debug: FIELD: " + type.getName() + " / " + fieldName + " = " + (mapped != null ? mapped : fieldName));
        
        // Check our MappingResolver
        if (fieldName.equalsIgnoreCase(res) && type.getName().contains("class_")) {
        	String clazz = myMappingResolver.unmapClassName("named", type.getName()); // Running -> Mojang
        	String mapp = myMappingResolver.mapFieldName_CheckObf("named", clazz, fieldName, null);
        	
        	dewe(type, "MAPP: " + mapp);
        	
        	// System.out.println("DeBUGff: " + type.getName() + ": " + key + " = " + res + " -> " + mapp);
        	if (mapp.contains("field_") || mapp.contains("comp_")) {
        		return mapp;
        	} else {
        		
        		
        		
        		// System.out.println("DeBUGf: " + type.getName() + ": " + key + " = " + res);
        	}
        } else {
        	//if (type.getName().contains("minecraft"))
        	//	System.out.println("DeBUGf: " + type.getName() + ": " + key + " = " + res);
        }
        
        //myMappingResolver.mapFieldName(res, mapped, fieldName, res)
        
        return mapped != null ? mapped : fieldName;
    }
    
    private static void dewe(Class<?> cl, String s) {
    	if (CardboardConfig.DEBUG_VERBOSE_CALLS) {
    		System.out.println("dewe: " +s);
    	}
    }

    @Override
    public String inverseMapFieldName(Class<?> type, String fieldName) {
        return jarMapping.fastReverseMapFieldName(type, fieldName);
    }

    @Override
    public String inverseMapName(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? clazz.getName() : mapping.getNmsName();
    }

    @Override
    public String inverseMapSimpleName(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? clazz.getSimpleName() : mapping.getNmsSimpleName();
    }

    @Override
    public String getClassDescriptor(Class<?> clazz, String name) {
        if (clazz.isArray()) {
            return "[" + getClassDescriptor(clazz.getComponentType(), name);
        } else if (clazz.isPrimitive()) {
            if (clazz == void.class) return "V";
            if (clazz == boolean.class) return "Z";
            if (clazz == byte.class) return "B";
            if (clazz == char.class) return "C";
            if (clazz == short.class) return "S";
            if (clazz == int.class) return "I";
            if (clazz == long.class) return "J";
            if (clazz == float.class) return "F";
            if (clazz == double.class) return "D";
        } else {
        	
        	if (!name.isEmpty()) {
        		// String in = jt.getInternalName().replace('/', '.');
    			String ll = mr.unmapClassName(name, clazz.getName().replace('/', '.'));
    			return "L" + ll.replace('.', '/') + ";";
        	}
        	
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
        throw new IllegalArgumentException("Unsupported class: " + clazz);
    }

	@Override
	public String getClassDescriptorResolveName(String namespace, String name) {
		return mr.unmapClassName(namespace, name);
	}

	@Override
	public BannerJarRemapper getJarRemapper() {
		return jarRemapper;
	}


	@Override
	public boolean shouldExtraDebugLog() {
		return CardboardConfig.DEBUG_VERBOSE_CALLS;
	}
}
