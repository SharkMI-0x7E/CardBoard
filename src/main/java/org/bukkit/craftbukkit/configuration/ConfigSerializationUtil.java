package org.bukkit.craftbukkit.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.bukkit.craftbukkit.CraftRegistry;

public final class ConfigSerializationUtil {
    public static String getString(Map<?, ?> map, String key, boolean nullable) {
        return ConfigSerializationUtil.getObject(String.class, map, key, nullable);
    }

    public static UUID getUuid(Map<?, ?> map, String key, boolean nullable) {
        String uuidString = ConfigSerializationUtil.getString(map, key, nullable);
        if (uuidString == null) {
            return null;
        }
        return UUID.fromString(uuidString);
    }

    public static <T> T getObject(Class<T> clazz, Map<?, ?> map, String key, boolean nullable) {
        Object object = map.get(key);
        if (clazz.isInstance(object)) {
            return clazz.cast(object);
        }
        if (object == null) {
            if (!nullable) {
                throw new NoSuchElementException(String.valueOf(map) + " does not contain " + key);
            }
            return null;
        }
        throw new IllegalArgumentException(key + "(" + String.valueOf(object) + ") is not a valid " + String.valueOf(clazz));
    }

    public static void setHolderSet(Map<String, Object> result, String key, HolderSet<?> holders) {
        holders.unwrap().ifLeft(tag -> result.put(key, "#" + tag.location().toString())).ifRight(list -> result.put(key, list.stream().map(entry -> entry.unwrapKey().orElseThrow().identifier().toString()).toList()));
    }

    public static <T> HolderSet<T> getHolderSet(Object from, ResourceKey<Registry<T>> registryKey) {
        String parseString;
        Registry<T> registry = CraftRegistry.getMinecraftRegistry(registryKey);
        if (from instanceof String && (parseString = (String)from).startsWith("#")) {
            Optional tag;
            Identifier key = Identifier.tryParse(parseString = parseString.substring(1));
            if (key != null && (tag = registry.get(TagKey.create(registryKey, key))).isPresent()) {
                return (HolderSet)tag.get();
            }
        } else {
            if (from instanceof List) {
                List parseList = (List)from;
                ArrayList holderList = new ArrayList(parseList.size());
                for (Object entry : parseList) {
                    Identifier key = Identifier.tryParse(entry.toString());
                    if (key == null) continue;
                    registry.get(key).ifPresent(holderList::add);
                }
                return HolderSet.direct(holderList);
            }
            throw new IllegalArgumentException("(" + String.valueOf(from) + ") is not a valid String or List");
        }
        return HolderSet.empty();
    }

    private ConfigSerializationUtil() {
    }
}

