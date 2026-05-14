package org.bukkit.craftbukkit.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

public class CraftNBTTagConfigSerializer {

    private static final Pattern ARRAY = Pattern.compile("^\\[.*]");
    private static final Pattern INTEGER = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)?i", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOUBLE = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);
    // private static final StringNbtReader MOJANGSON_PARSER = new StringNbtReader(new StringReader(""));
    private static final TagParser<Tag> MOJANGSON_PARSER = TagParser.create(NbtOps.INSTANCE);
    
    
    public static String serialize(Tag tag) {
        SnbtPrinterTagVisitor snbtVisitor = new SnbtPrinterTagVisitor();
        return snbtVisitor.visit(tag);
    }
    
    public static Tag deserialize(Object object) {
        if (object instanceof String) {
            String snbtString = (String)object;
            try {
                return TagParser.parseCompoundFully(snbtString);
            }
            catch (CommandSyntaxException e2) {
                throw new RuntimeException("Failed to deserialise nbt", e2);
            }
        }
        return CraftNBTTagConfigSerializer.internalLegacyDeserialization(object);
    }
    
    private static Tag internalLegacyDeserialization(@NotNull Object object) {
        if (object instanceof Map) {
            CompoundTag compound = new CompoundTag();
            for (Map.Entry entry : ((Map<String, Object>)object).entrySet()) {
                compound.put((String)entry.getKey(), CraftNBTTagConfigSerializer.internalLegacyDeserialization(entry.getValue()));
            }
            return compound;
        }
        if (object instanceof List) {
            List list = (List)object;
            if (list.isEmpty()) {
                return new ListTag();
            }
            ListTag tagList = new ListTag();
            for (Object tag : list) {
                tagList.add(CraftNBTTagConfigSerializer.internalLegacyDeserialization(tag));
            }
            return tagList;
        }
        if (object instanceof String) {
            String string = (String)object;
            if (ARRAY.matcher(string).matches()) {
                try {
                    return MOJANGSON_PARSER.parseAsArgument(new StringReader(string));
                }
                catch (CommandSyntaxException e2) {
                    throw new RuntimeException("Could not deserialize found list ", e2);
                }
            }
            if (INTEGER.matcher(string).matches()) {
                return IntTag.valueOf(Integer.parseInt(string.substring(0, string.length() - 1)));
            }
            if (DOUBLE.matcher(string).matches()) {
                return DoubleTag.valueOf(Double.parseDouble(string.substring(0, string.length() - 1)));
            }
            try {
                Tag tag = MOJANGSON_PARSER.parseAsArgument(new StringReader(string));
                if (tag instanceof IntTag) {
                    return StringTag.valueOf(tag.toString());
                }
                if (tag instanceof DoubleTag) {
                    return StringTag.valueOf(String.valueOf(((DoubleTag)tag).doubleValue()));
                }
                if (tag instanceof StringTag) {
                    return StringTag.valueOf(string);
                }
                return tag;
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new RuntimeException("Could not deserialize found primitive ", commandSyntaxException);
            }
        }
        throw new RuntimeException("Could not deserialize Tag");
    }
    
    /*
    public static Object serialize(NbtElement base) {
        if (base instanceof NbtCompound) {
            Map<String, Object> innerMap = new HashMap<>();
            for (String key : ((NbtCompound) base).getKeys()) innerMap.put(key, serialize(((NbtCompound) base).get(key)));
            return innerMap;
        } else if (base instanceof NbtList) {
            List<Object> baseList = new ArrayList<>();
            for (int i = 0; i < ((AbstractNbtList<?>) base).size(); i++) baseList.add(serialize((NbtElement) ((AbstractNbtList<?>) base).get(i)));
            return baseList;
        }
        return (base instanceof NbtString) ? base.asString() : ((base instanceof NbtInt) ? base.toString() + "i" : base.toString());
    }

    @SuppressWarnings("unchecked")
    public static NbtElement deserialize(Object object) {
        if (object instanceof Map) {
            NbtCompound compound = new NbtCompound();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) object).entrySet()) compound.put(entry.getKey(), deserialize(entry.getValue()));
            return compound;
        } else if (object instanceof List) {
            List<Object> list = (List<Object>) object;
            if (list.isEmpty()) return new NbtList(); // default

            NbtList tagList = new NbtList();
            for (Object tag : list) tagList.add(deserialize(tag));
            return tagList;
        } else if (object instanceof String) {
            String string = (String) object;

            if (ARRAY.matcher(string).matches()) {
                try {
                    return new StringNbtReader(new StringReader(string)).parseElementPrimitiveArray();
                } catch (CommandSyntaxException e) {throw new RuntimeException("Could not deserialize found list ", e);}
            } else if (INTEGER.matcher(string).matches()) { //Read integers on our own
                return NbtInt.of(Integer.parseInt(string.substring(0, string.length() - 1)));
            } else if (DOUBLE.matcher(string).matches()) {
                return NbtDouble.of(Double.parseDouble(string.substring(0, string.length() - 1)));
            } else {
                NbtElement Tag = MOJANGSON_PARSER.parsePrimitive(string);

                if (Tag instanceof NbtInt) { // If this returns an integer, it did not use our method from above
                    return NbtString.of(Tag.asString()); // It then is a string that was falsely read as an int
                } else if (Tag instanceof NbtDouble) return NbtString.of(String.valueOf(((NbtDouble) Tag).doubleValue())); // Doubles add "d" at the end
                else return Tag;
            }
        }
        throw new RuntimeException("Could not deserialize Tag");
    }*/

}