/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cardboardpowered.util.nms;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class MyMappingResolver implements MappingResolver {

	public static MemoryMappingTree tree;
    private final String fromNamespace;
    private final String toNamespace;
    
    private final int targetNamespaceId;

    public MyMappingResolver(Path mappingFile, MappingFormat format, String fromNamespace, String toNamespace) throws IOException {
        if (null == tree) {
    	tree = new MemoryMappingTree();
        // mapping-io can read Tiny v2, Enigma, etc.
        // net.fabricmc.mappingio.format.MappingFormat format =
        //        net.fabricmc.mappingio.format.MappingFormat..detect(mappingFile);
        net.fabricmc.mappingio.MappingReader.read(mappingFile, format, tree);
        }

        this.fromNamespace = Objects.requireNonNull(fromNamespace);
        this.toNamespace = Objects.requireNonNull(toNamespace);
        
        this.targetNamespaceId = tree.getNamespaceId(toNamespace);
    }

	@Override
	public Collection<String> getNamespaces() {
		HashSet<String> namespaces = new HashSet<>(tree.getDstNamespaces());
		namespaces.add(tree.getSrcNamespace());
		return Collections.unmodifiableSet(namespaces);
	}

	@Override
	public String getCurrentRuntimeNamespace() {
		return toNamespace;
	}

	@Override
	public String mapClassName(String namespace, String className) {
		if (className.indexOf('/') >= 0) {
			throw new IllegalArgumentException("Class names must be provided in dot format: " + className);
		}

		return replaceSlashesWithDots(tree.mapClassName(replaceDotsWithSlashes(className), tree.getNamespaceId(namespace), targetNamespaceId));
	}
	
	public String mapClassName2(String namespace, String className, String toNamespace) {
		if (className.indexOf('/') >= 0) {
			throw new IllegalArgumentException("Class names must be provided in dot format: " + className);
		}

		return replaceSlashesWithDots(
				tree.mapClassName(replaceDotsWithSlashes(className), tree.getNamespaceId(namespace), tree.getNamespaceId(toNamespace)));
	}

	@Override
	public String unmapClassName(String namespace, String className) {
		if (className.indexOf('/') >= 0) {
			throw new IllegalArgumentException("Class names must be provided in dot format: " + className);
		}

		return replaceSlashesWithDots(tree.mapClassName(replaceDotsWithSlashes(className), targetNamespaceId, tree.getNamespaceId(namespace)));
	}

	@Override
	public String mapFieldName(String namespace, String owner, String name, String descriptor) {
		if (owner.indexOf('/') >= 0) {
			throw new IllegalArgumentException("Class names must be provided in dot format: " + owner);
		}

		MappingTree.FieldMapping field = tree.getField(replaceDotsWithSlashes(owner), name, descriptor, tree.getNamespaceId(namespace));

		return field == null ? name : field.getName(targetNamespaceId);
	}
	
	public String mapFieldName_CheckObf(String namespace, String owner, String name, String descriptor) {
		if (owner.indexOf('/') >= 0) {
			throw new IllegalArgumentException("Class names must be provided in dot format: " + owner);
		}

		MappingTree.FieldMapping field = tree.getField(replaceDotsWithSlashes(owner), name, descriptor, tree.getNamespaceId(namespace));

		// 
		if (null == field) {
			String cl = mapClassName2("intermediary", mapClassName("named", owner), "official");

			MappingTree.FieldMapping field2 = tree.getField(replaceDotsWithSlashes(cl), name, descriptor, tree.getNamespaceId("official"));
			if (null != field2) {
				field = field2;
			}
		}
		
		return field == null ? name : field.getName(targetNamespaceId);
	}
	
	public String runtime(String a) {
		return mapClassName("named", a);
	}

	@Override
	public String mapMethodName(String namespace, String owner, String name, String descriptor) {
		if (owner.indexOf('/') >= 0) {
			throw new IllegalArgumentException("Class names must be provided in dot format: " + owner);
		}

		MappingTree.MethodMapping method = tree.getMethod(replaceDotsWithSlashes(owner), name, descriptor, tree.getNamespaceId(namespace));
		
        if (null == method) {
        	String run = mapClassName("named", owner);
        	String cl = mapClassName2("intermediary", run, "official");

        	MappingTree.MethodMapping method2 = tree.getMethod(replaceDotsWithSlashes(cl), name, descriptor, tree.getNamespaceId("official"));
        	if (null != method2) {
        		method = method2;
        	}
        	if (null == method2) {
        		// ClassMapping cm = tree.getClass(replaceDotsWithSlashes(cl), tree.getNamespaceId("official"));
        		try {
					List<String> supers = getSuperClasses(replaceDotsWithSlashes(run), "intermediary");
					for (var s : supers) {
						// this.mapClassName2(namespace, name, namespace)
						
						String run1 = s.replace('/', '.'); //mapClassName("named", s.replace('/', '.'));
			        	String cl1 = run1; // mapClassName2("intermediary", run1, "official");
			        	
			        	String nam = mapMethodName(namespace, cl1, name, descriptor);

			        	if (nam.startsWith("method_")) {
			        		return nam;
			        	}
			        	
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		//tree.getClass(name, targetNamespaceId)
        	}
        }
		
		return method == null ? name : method.getName(targetNamespaceId);
	}

	private static boolean DEV = false;
	
	  /**
     * @param tree The loaded MemoryMappingTree
     * @param className The name of the class (e.g., "net.minecraft.class_123")
     * @param namespace The namespace of the input name (e.g., "intermediary")
     * @return A list of all parent class names in the same namespace
     */
    public List<String> getSuperClasses(String className, String namespace) throws Exception {
        if (DEV) {
        	className = this.mapClassName2("intermediary", className.replace('/', '.'), "named").replace('.', '/');
        }
        
    	List<String> superClasses = new ArrayList<>();
        
        // 1. Convert the mapped name to a standard Java binary name
        String binaryName = className.replace('/', '.');
        
        // 2. Load the class to traverse the hierarchy via Reflection
        Class<?> currentClass = Class.forName(binaryName, false, this.getClass().getClassLoader());
        
        // 3. Walk up the hierarchy
        while (currentClass.getSuperclass() != null) {
            currentClass = currentClass.getSuperclass();
            String internalName = currentClass.getName().replace('.', '/');
            
            // 4. Look up the parent in the MappingTree to get the name in your namespace
            MappingTree.ClassMapping parentMapping = tree.getClass(internalName);
            
            if (parentMapping != null) {
                // Return the name in the requested namespace
                superClasses.add(parentMapping.getName(namespace));
            } else {
                // If not found in mappings (e.g. java.lang.Object), use the internal name
                superClasses.add(internalName);
            }
        }
        
        return superClasses;
    }
	
	/**
     * Resolves a method name safely, automatically remapping descriptors
     * into the fromNamespace before lookup.
     *
     * @param ownerClass The class name in fromNamespace
     * @param methodName The method name in fromNamespace
     * @param methodDesc The method descriptor (any namespace)
     * @param descNamespace The namespace the descriptor is currently in
     * @return The mapped method name in toNamespace
     */
    public String mapMethodNameSafe(String namespace, String ownerClass, String methodName, String methodDesc, String descNamespace) {
        // Ensure descriptor is in fromNamespace
    	String descInFrom = methodDesc;
        if (!descNamespace.equals(namespace)) {
            descInFrom = tree.mapDesc(methodDesc, tree.getNamespaceId(descNamespace), tree.getNamespaceId(namespace));
        }

        var cls = tree.getClass(ownerClass, tree.getNamespaceId(namespace));
        if (cls == null) return methodName;

        var method = cls.getMethod(methodName, descInFrom, tree.getNamespaceId(namespace));
        
        if (method == null) {
        	return this.mapMethodName(namespace, ownerClass, methodName, descInFrom);
        }
        
        return method != null ? method.getName(targetNamespaceId) : methodName;
    }
    
	/**
     * Resolves a method name safely, automatically remapping descriptors
     * into the fromNamespace before lookup.
     *
     * @param ownerClass The class name in fromNamespace
     * @param methodName The method name in fromNamespace
     * @param methodDesc The method descriptor (any namespace)
     * @param descNamespace The namespace the descriptor is currently in
     * @return The mapped method name in toNamespace
     */
    public String mapMethodNameSafe_CheckObf(String namespace, String ownerClass, String methodName, String methodDesc, String descNamespace) {
        // Ensure descriptor is in fromNamespace
        String descInFrom = methodDesc;
        if (!descNamespace.equals(namespace)) {
            descInFrom = tree.mapDesc(methodDesc, tree.getNamespaceId(descNamespace), tree.getNamespaceId(namespace));
        }

        var cls = tree.getClass(ownerClass, tree.getNamespaceId(namespace));
        if (cls == null) {
        	
        	return methodName;
        }

        var method = cls.getMethod(methodName, descInFrom, tree.getNamespaceId(namespace));
       
        /*
        if (null == method) {
        	String cl = mapClassName2("intermediary", mapClassName("named", ownerClass), "official");
        }
        */
        
        /*
		if (null == field) {
			String cl = mapClassName2("intermediary", mapClassName("named", owner), "official");

			MappingTree.FieldMapping field2 = tree.getField(replaceDotsWithSlashes(cl), name, descriptor, tree.getNamespaceId("official"));
			if (null != field2) {
				field = field2;
			}
		}
         */
        
        return method != null ? method.getName(targetNamespaceId) : methodName;
    }

	private static String replaceSlashesWithDots(String cname) {
		return cname.replace('/', '.');
	}

	private static String replaceDotsWithSlashes(String cname) {
		return cname.replace('.', '/');
	}
}
