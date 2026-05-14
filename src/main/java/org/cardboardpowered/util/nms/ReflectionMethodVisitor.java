/**
 * Cardboard - Bukkit/Spigot/Paper API for Fabric
 * Copyright (C) 2023-2026, CardboardPowered.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.util.nms;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.techcable.srglib.JavaType;
import net.techcable.srglib.MethodSignature;

import org.bukkit.Material;
import org.bukkit.craftbukkit.util.Commodore;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.impl.util.CardboardMagicNumbers;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.cardboardpowered.CardboardMod;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ReflectionMethodVisitor extends MethodVisitor {

    public static ArrayList<String> SKIP = new ArrayList<>();
    static {
        SKIP.add("vault");
        SKIP.add("worldguard");
    }

    private String pln;
    private MappingResolver mr;

    public ReflectionMethodVisitor(int api, MethodVisitor visitMethod, String pln) {
        super(api, visitMethod);
        this.pln = pln;

        this.mr = FabricLoader.getInstance().getMappingResolver();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (CardboardConfig.DEBUG_VERBOSE_CALLS) {
        	if (!owner.startsWith("java/")) {
        		CardboardMod.LOGGER.info(owner + " / " + name);
        	}
        }
    	
    	if (owner.equalsIgnoreCase("org/bukkit/Material")) {
            if (CardboardMagicNumbers.MODDED_MATERIALS.containsKey(name)) {
				System.out.println("Modded Material Debug: " + name);
                super.visitFieldInsn( opcode, owner, "STONE", desc );
                return;
            }
        }
        
        if (owner.startsWith("net/minecraft") && name.length() <= 2) {
        	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 

        	// TODO: fix "$" support
        	
        	if (owner.contains("class_31$a")) {
        		owner =  owner.replace("class_31$a", "class_31$class_7729");
        		if (name.equalsIgnoreCase("b")) {
        			name = "field_40374";
        		}
        		if (name.equalsIgnoreCase("a")) {
        			name = "field_40373";
        		}
        		if (name.equalsIgnoreCase("c")) {
        			name = "field_40375";
        		}
        	}
        	
        	if (desc.contains("class_31$a")) {
        		desc = desc.replace("class_31$a", "class_31$class_7729");
        	}
        	
        	String owner_official = mr.unmapClassName("official", owner.replace('/', '.'));

        	String sigg = "";

        	JavaType jt  = JavaType.fromDescriptor(desc);
        	
        	if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
    			String in = jt.getInternalName().replace('/', '.');
    			String ll = mr.unmapClassName("official", in);
    			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
    		} else {
    			sigg += jt.getDescriptor();
    		}

        	String mapped = mr.mapFieldName("official", owner_official, name, sigg);
        	
        	if (!mapped.startsWith("field_")) {
        		
        		
        		String res = mapped;
        		try {
        			Class<?> up = Class.forName(owner.replace('/', '.'));
        			
        	    	String in = find_in_inheritance_f(up, res, desc, sigg);
        	    	if (in.startsWith("field_")) {
        	    		res = in;
        	    	}
        			
        			if (null != up.getSuperclass()) {
            			//String supn = up.getSuperclass().getName();
            			//res = do_map(supn, name, desc);
        			}
        			
        			if (!res.startsWith("field_")) {
        				// Check interface
        			}
        			
        	    	if (!res.startsWith("field_")) {
        	    		String inn = find_in_inheritance_f(up, res, desc, sigg);
        	    		if (inn.startsWith("field_")) {
        	    			res = in;
        	    		}
        	    	}
        			
        		} catch (ClassNotFoundException e) {
        			CardboardMod.LOGGER.finest("MISSING CLASS MAPPING FOR: " + owner);
        			System.out.println(e.getMessage());
        		} catch (Exception e) {
        			// Oh no!
        			e.printStackTrace();
        		}

        		// System.out.println("\tFIELD:: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped);
        	}

        	if (mapped.equalsIgnoreCase("field_41255")) {
        		// Note: Find out why this is being mapped wrong in the worldedit adaptor
        		mapped = "field_41254";
        	}

        	if (mapped.equalsIgnoreCase("field_41199")) {
        		mapped = "field_41197";
        	}

        	super.visitFieldInsn( opcode, owner, mapped, desc );
        	return;
        }
        
    	if (name.equalsIgnoreCase("field_41255")) {
    		// Note: Find out why this is being mapped wrong in the worldedit adaptor
    		name = "field_41254";
    	}

    	if (name.equalsIgnoreCase("field_41199")) {
    		// Note: Find out why this is being mapped wrong in the worldedit adaptor
    		name = "field_41197";
    	}

        super.visitFieldInsn( opcode, owner, name, desc );
    }

    public static Field Material_getField(String name) throws NoSuchFieldException, SecurityException {
        try {
            return Material.class.getField(name);
        } catch (NoSuchFieldException | SecurityException e) {
			System.out.println("STONE:? " + e.getMessage());
            return Material.class.getField("STONE");
        }
    }

    public static String do_map(String owner, String name, String desc) {
    	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 

    	String owner_official = mr.unmapClassName("official", owner.replace('/', '.'));
    	
    	MethodSignature sig = MethodSignature.fromDescriptor(desc);

    	List<JavaType> jts = sig.getParameterTypes();
    	
    	String sigg = "(";
    	
    	for (JavaType jt : jts) {
    		if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
    			String in = jt.getInternalName().replace('/', '.');
    			String ll = mr.unmapClassName("official", in);
    			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
    		} else {
    			sigg += jt.getDescriptor();
    		}
    	}

    	sigg += ")";
    	
    	JavaType jt  = sig.getReturnType();
    	
    	if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
			String in = jt.getInternalName().replace('/', '.');
			String ll = mr.unmapClassName("official", in);
			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
		} else {
			sigg += jt.getDescriptor();
		}

    	String mapped = mr.mapMethodName("official", owner_official, name, sigg);
    	
    	
    	if (!mapped.startsWith("method_")) {
    		
    		String res = mapped;

    		// Check super class:
    		try {
    			Class<?> up = Class.forName(owner.replace('/', '.'));
    			
    	    	String in = find_in_inheritance(up, res, desc, sigg);
    	    	if (in.startsWith("method_")) {
    	    		return in;
    	    	}
    			
    			if (null != up.getSuperclass()) {
        			String supn = up.getSuperclass().getName();
        			res = do_map(supn, name, desc);
    			}
    			
    			if (!res.startsWith("method_")) {
    				// Check interface
    			}
    			
    	    	if (!res.startsWith("method_")) {
    	    		String inn = find_in_inheritance(up, res, desc, sigg);
    	    		if (inn.startsWith("method_")) {
    	    			return in;
    	    		}
    	    	}
    			
    		} catch (ClassNotFoundException e) {
    			CardboardMod.LOGGER.finest("MISSING CLASS MAPPING FOR: " + owner);
    			System.out.println("ClassNotFound: " + e.getMessage());
    		} catch (Exception e) {
    			// Oh no!
    			e.printStackTrace();
    		}
    		
    		/*
    		if (!res.startsWith("method_")) {
    			//	System.out.println("TESTING: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped + " (" + res + ")");
    		}
    		*/
    		mapped = res;
    		
    	}

    	//if (!mapped.startsWith("method_")) {
    	//	System.out.println("TESTING: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped);
    	//}
    	return mapped;
    }
    
    public static String find_in_inheritance(Class<?> clazz, String obf_name, String desc, String sigg) {
    	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 
    	
		String owner_official = mr.unmapClassName("official", clazz.getName().replace('/', '.'));
		
		String mapped = mr.mapMethodName("official", owner_official, obf_name, desc);

		if (mapped.startsWith("method_")) {
			//System.out.println("FOUND IN INTERFACES! = " + mapped);
			return mapped;
		}
		
		String mapped2 = mr.mapMethodName("official", owner_official, obf_name, sigg);

		if (mapped2.startsWith("method_")) {
			//System.out.println("FOUND IN INTERFACES! = " + mapped2);
			return mapped2;
		}
    	
    	for (Class<?> ih : clazz.getInterfaces()) {
    		String in = find_in_inheritance(ih, obf_name, desc, sigg);
    		if (in.startsWith("method_")) {
    			return in;
    		}
    	}
    	return obf_name;
    }
    
    public static String find_in_inheritance_f(Class<?> clazz, String obf_name, String desc, String sigg) {
    	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 
    	
		String owner_official = mr.unmapClassName("official", clazz.getName().replace('/', '.'));
		
		String mapped = mr.mapFieldName("official", owner_official, obf_name, desc);

		if (mapped.startsWith("field_")) {
			//System.out.println("FOUND IN INTERFACES! = " + mapped);
			return mapped;
		}
		
		String mapped2 = mr.mapFieldName("official", owner_official, obf_name, sigg);

		if (mapped2.startsWith("field_")) {
			//System.out.println("FOUND IN INTERFACES! = " + mapped2);
			return mapped2;
		}
    	
    	for (Class<?> ih : clazz.getInterfaces()) {
    		String in = find_in_inheritance(ih, obf_name, desc, sigg);
    		if (in.startsWith("field_")) {
    			return in;
    		}
    	}
    	return obf_name;
    }

    private void debug(String o) {
    	if (CardboardConfig.DEBUG_VERBOSE_CALLS) {
    		CardboardMod.LOGGER.info(o);
    	}
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (CardboardConfig.DEBUG_VERBOSE_CALLS) {
        	if (!owner.startsWith("java/")) {
        		CardboardMod.LOGGER.info(owner + " / " + name);
        	}
        }

		// Redirect WorldGuard Custom Logger (JUL->SLF4J)
        if (owner.contains("com/sk89q/worldguard/util/logging/RecordMessagePrefixer") ) {
        	owner = owner.replace("com/sk89q/worldguard/util/logging/RecordMessagePrefixer", "org/cardboardpowered/util/RecordMessagePrefixer");
        }

        if (owner.contains("LegacyPotionMetaProvider")) {
        	debug(owner + " " + name + " " + desc);
        	owner = owner.replace("LegacyPotionMetaProvider", "ModernPotionMetaProvider");
        }

        if (owner.startsWith("org/bukkit/craftbukkit") && owner.contains(ReflectionRemapper.NMS_VERSION)) {
        	System.out.println("Stripping version package (" + ReflectionRemapper.NMS_VERSION + ") from org/bukkit/craftbukkit reference.");
        	owner = owner.replace("org/bukkit/craftbukkit/" + ReflectionRemapper.NMS_VERSION + "/", "org/bukkit/craftbukkit/");
        }
        
        if (owner.startsWith("net/minecraft") && name.equals("getMinecraftServer")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getNmsServer", desc, false );
            return;
        }

        if (owner.startsWith("net/minecraft") && owner.contains("MinecraftServer") && name.equals("getServer")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getNmsServer", desc, false );
            return;
        }
        
        if (owner.startsWith("net/minecraft") && (owner.contains("DedicatedServer") || owner.contains("class_3176")) && name.equals("getServer")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getNmsServer", desc, false );
            return;
        }
        
        if (name.contains("method_45136")) {
        	if (opcode == Opcodes.INVOKESTATIC) {
        		// Give us the static method
        		name = "method_12829";
        	}
        }
        
        // Try Registry.
        if (owner.contains("class_2359") || desc.contains("class_2359")) {
        	// System.out.println("IDMAP FOUD: " + owner + " / " + name + " / " + desc);
        	owner = owner.replace("class_2359", "class_2378");
        	desc = desc.replace("class_2359", "class_2378");
        }

        if (name.equals("lookupOrThrow")) {
        	System.out.println(opcode);
        	System.out.println("O: " + owner);
        	System.out.println("desc: " + desc);
        	name = "cardboard$" + name;
        }

        if (opcode == Opcodes.INVOKEINTERFACE
                && owner.equals("net/minecraft/class_5455$class_6890") // RegistryAccess$Frozen
                && name.equals("lookupOrThrow")
                && desc.equals("(Lnet/minecraft/class_5321;)Lnet/minecraft/class_2359;")) { // IdMap


            super.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    owner,
                    "cardboard$lookupOrThrow",
                    "(Lnet/minecraft/class_5321;)Lnet/minecraft/class_2378;",
                    true
            );
            super.visitTypeInsn(Opcodes.CHECKCAST, "net/minecraft/class_2378"); // Registry

            return;
        }

        if (owner.startsWith("net/minecraft") && name.length() <= 2) {
        	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 

        	String owner_official = mr.unmapClassName("official", owner.replace('/', '.'));
        	
        	MethodSignature sig = MethodSignature.fromDescriptor(desc);
        	
        	List<JavaType> jts = sig.getParameterTypes();
        	
        	String sigg = "(";
        	
        	for (JavaType jt : jts) {
        		if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
        			String in = jt.getInternalName().replace('/', '.');
        			String ll = mr.unmapClassName("official", in);
        			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
        		} else {
        			sigg += jt.getDescriptor();
        		}
        	}

        	sigg += ")";
        	
        	JavaType jt  = sig.getReturnType();
        	
        	if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
    			String in = jt.getInternalName().replace('/', '.');
    			String ll = mr.unmapClassName("official", in);
    			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
    		} else {
    			sigg += jt.getDescriptor();
    		}

        	String mapped = mr.mapMethodName("official", owner_official, name, sigg);
        	
        	if (!mapped.startsWith("method_") && (name.equalsIgnoreCase(mapped))) {
        		
        		String res = mapped;

        		// Check super class:
        		try {
        			Class<?> up = Class.forName(owner.replace('/', '.'));

        	    	String in = find_in_inheritance(up, res, desc, sigg);
        	    	if (in.startsWith("method_")) {
        	    		res = in;
        	    	} else if (null != up.getSuperclass()) {
	        			String supn = up.getSuperclass().getName();
	        			res = do_map(supn, name, desc);
        			}
        		} catch (ClassNotFoundException e) {
        			CardboardMod.LOGGER.finest("MISSING CLASS MAPPING FOR: " + owner);
        			System.out.println(e.getMessage());
        		} catch (Exception e) {
        			// Oh no!
        			e.printStackTrace();
        		}
        		
        		if (res.equalsIgnoreCase("I_") && desc.contains("Liu")) {
        			// res = "method_30349";
        		}
        		
        		// TODO Support classes with "$"
        		if (res.equalsIgnoreCase("f") && desc.contains("class_5321") && owner.contains("class_2378")) {
        			res = "method_40290";
        		}
        		
        		if (!res.startsWith("method_")) {
        			//System.out.println("\tMETHOD: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped + " (" + res + ")");
        		}
        		mapped = res;
        		
        	}

        	super.visitMethodInsn( opcode, owner, mapped, desc, itf );
        	return;
        }

        if (owner.contains("NbtCompound") || owner.contains("class_2487")) {
            if (name.startsWith("setString")) {
                String cl = mr.unmapClassName("intermediary", owner.replace('/','.'));
                String name2 = mr.mapMethodName("intermediary", cl.replace('/', '.'), "method_10582", desc);
                super.visitMethodInsn( opcode, owner, name2, desc, false );
                return;
            }
        }

        if (owner.equalsIgnoreCase("org/bukkit/Material")) {
            if (name.equalsIgnoreCase("getField")) {
                // System.out.println("\nGET MATERIAL FIELD!!!!!\n");
                super.visitFieldInsn( opcode, "org/cardboardpowered/util/nms/ReflectionMethodVisitor", "Material_getField", desc );
                return;
            }
        }

        if (owner.equalsIgnoreCase("com/comphenix/protocol/utility/MinecraftReflection")) {
            // System.out.println("PROTOCOLLIB REFLECTION: " + name);
            if (name.equals("getCraftBukkitClass") || name.equals("getMinecraftClass")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ProtocolLibMapper", name, desc, false );
                return;
            }
        }

        if (owner.equalsIgnoreCase("com/comphenix/protocol/injector/netty/ChannelInjector")) {
            if (name.equals("guessCompression")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ProtocolLibMapper", name, desc, false );
                return;
            }
        }
        
        if (owner.equalsIgnoreCase("com/sk89q/worldguard/bukkit/util/Materials")) {
            if (name.equals("isSpawnEgg") || name.equals("getEntitySpawnEgg") || name.equals("isArmor") ||
                    name.equals("isToolApplicable") || name.equals("isWaxedCopper")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/WorldGuardMaterialHelper", name, desc, false );
                return;
            }
        }
        
        if (owner.startsWith("net/minecraft") && name.startsWith("method_")) {
        	String namespace = mr.getCurrentRuntimeNamespace();
        	if (namespace.equalsIgnoreCase("named")) {
        		name = mr.mapMethodName("intermediary", owner.replace('/', '.'), name, desc);
        	}
        }

        for (String str : SKIP) {
            if (this.pln.equalsIgnoreCase(str) || owner.startsWith("org/bukkit")) {
                // Skip Vault cause weird things happen
                super.visitMethodInsn( opcode, owner, name, desc, itf );
                return;
            }
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("forName") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/Class;"))
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "mapClassName", "(Ljava/lang/String;)Ljava/lang/String;", false);
        
        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethods")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getMethods", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.startsWith("net/minecraft/class_")) {
            if (owner.equalsIgnoreCase("net/minecraft/class_3176") && name.equalsIgnoreCase("getVersion")) {
                // Add MinecraftServer#getVersion
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getMinecraftServerVersion", "()Ljava/lang/String;", false);
                return;
            }
        }
        
        owner = Commodore.getOriginalOrRewrite(owner);

        super.visitMethodInsn( opcode, owner, name, desc, itf );
    }

}
