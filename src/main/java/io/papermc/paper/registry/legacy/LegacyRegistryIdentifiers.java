package io.papermc.paper.registry.legacy;

import com.google.common.collect.ImmutableMap;
import io.leangen.geantyref.GenericTypeReflector;
import io.papermc.paper.registry.RegistryKey;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

@Deprecated
public final class LegacyRegistryIdentifiers {

    public static final Map<Class<?>, RegistryKey<?>> CLASS_TO_KEY_MAP;

    private LegacyRegistryIdentifiers() {
    }

    static {
    	ImmutableMap.Builder<Class<?>, RegistryKey<?>> builder = ImmutableMap.builder();
        try {
            for (Field field : RegistryKey.class.getFields()) {
                if (field.getType() != RegistryKey.class) continue;
                
                Class<?> legacyType = GenericTypeReflector.erase((Type)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0]);
                builder.put(legacyType, (RegistryKey<?>)field.get(null));
            }
        }
        catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        CLASS_TO_KEY_MAP = builder.build();
    }

}