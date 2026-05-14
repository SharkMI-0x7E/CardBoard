package org.bukkit.craftbukkit.persistence;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class CraftPersistentDataTypeRegistry {
    private final Function<Class, TagAdapter> CREATE_ADAPTER = this::createAdapter;
    private final Map<Class, TagAdapter> adapters = new ConcurrentHashMap<Class, TagAdapter>();

    private <T> TagAdapter createAdapter(Class<T> type) {
        if (!Primitives.isWrapperType(type)) {
            type = Primitives.wrap(type);
        }
        if (Objects.equals(Byte.class, type)) {
            return this.createAdapter(Byte.class, ByteTag.class, (byte)1, ByteTag::valueOf, ByteTag::value);
        }
        if (Objects.equals(Short.class, type)) {
            return this.createAdapter(Short.class, ShortTag.class, (byte)2, ShortTag::valueOf, ShortTag::value);
        }
        if (Objects.equals(Integer.class, type)) {
            return this.createAdapter(Integer.class, IntTag.class, (byte)3, IntTag::valueOf, IntTag::value);
        }
        if (Objects.equals(Long.class, type)) {
            return this.createAdapter(Long.class, LongTag.class, (byte)4, LongTag::valueOf, LongTag::value);
        }
        if (Objects.equals(Float.class, type)) {
            return this.createAdapter(Float.class, FloatTag.class, (byte)5, FloatTag::valueOf, FloatTag::value);
        }
        if (Objects.equals(Double.class, type)) {
            return this.createAdapter(Double.class, DoubleTag.class, (byte)6, DoubleTag::valueOf, DoubleTag::value);
        }
        if (Objects.equals(String.class, type)) {
            return this.createAdapter(String.class, StringTag.class, (byte)8, StringTag::valueOf, StringTag::value);
        }
        if (Objects.equals(byte[].class, type)) {
            return this.createAdapter(byte[].class, ByteArrayTag.class, (byte)7, array -> new ByteArrayTag(Arrays.copyOf(array, ((byte[])array).length)), n -> Arrays.copyOf(n.getAsByteArray(), n.size()));
        }
        if (Objects.equals(int[].class, type)) {
            return this.createAdapter(int[].class, IntArrayTag.class, (byte)11, array -> new IntArrayTag(Arrays.copyOf(array, ((int[])array).length)), n -> Arrays.copyOf(n.getAsIntArray(), n.size()));
        }
        if (Objects.equals(long[].class, type)) {
            return this.createAdapter(long[].class, LongArrayTag.class, (byte)12, array -> new LongArrayTag(Arrays.copyOf(array, ((long[])array).length)), n -> Arrays.copyOf(n.getAsLongArray(), n.size()));
        }
        if (Objects.equals(PersistentDataContainer[].class, type)) {
            return this.createAdapter(PersistentDataContainer[].class, ListTag.class, (byte)9, containerArray -> {
                ListTag list = new ListTag();
                for (PersistentDataContainer persistentDataContainer : containerArray) {
                    list.add(((CraftPersistentDataContainer)persistentDataContainer).toTagCompound());
                }
                return list;
            }, tag -> {
                PersistentDataContainer[] containerArray = new CraftPersistentDataContainer[tag.size()];
                for (int i2 = 0; i2 < tag.size(); ++i2) {
                    CraftPersistentDataContainer container = new CraftPersistentDataContainer(this);
                    CompoundTag compound = tag.getCompoundOrEmpty(i2);
                    for (String key : compound.keySet()) {
                        container.put(key, compound.get(key));
                    }
                    containerArray[i2] = container;
                }
                return containerArray;
            });
        }
        if (Objects.equals(PersistentDataContainer.class, type)) {
            return this.createAdapter(CraftPersistentDataContainer.class, CompoundTag.class, (byte)10, CraftPersistentDataContainer::toTagCompound, tag -> {
                CraftPersistentDataContainer container = new CraftPersistentDataContainer(this);
                for (String key : tag.keySet()) {
                    container.put(key, tag.get(key));
                }
                return container;
            });
        }
        if (Objects.equals(List.class, type)) {
            return this.createAdapter(List.class, ListTag.class, (byte)9, this::constructList, this::extractList, this::matchesListTag);
        }
        throw new IllegalArgumentException("Could not find a valid TagAdapter implementation for the requested type " + type.getSimpleName());
    }

    private <T, Z extends Tag> TagAdapter<T, Z> createAdapter(Class<T> primitiveType, Class<Z> tagType, byte nmsTypeByte, Function<T, Z> builder, Function<Z, T> extractor) {
        return this.createAdapter(primitiveType, tagType, nmsTypeByte, (type, t) -> builder.apply(t), (type, z) -> extractor.apply(z), (type, t) -> tagType.isInstance(t));
    }

    private <T, Z extends Tag> TagAdapter<T, Z> createAdapter(Class<T> primitiveType, Class<Z> tagType, byte nmsTypeByte, BiFunction<PersistentDataType<T, ?>, T, Z> builder, BiFunction<PersistentDataType<T, ?>, Z, T> extractor, BiPredicate<PersistentDataType<T, ?>, Tag> matcher) {
        return new TagAdapter<T, Z>(primitiveType, tagType, nmsTypeByte, builder, extractor, matcher);
    }

    public <T> Tag wrap(PersistentDataType<T, ?> type, T value) {
        return this.getOrCreateAdapter(type).build(type, value);
    }

    public <T> boolean isInstanceOf(PersistentDataType<T, ?> type, Tag base) {
        return this.getOrCreateAdapter(type).isInstance(type, base);
    }

    @NotNull
    private <T, Z extends Tag> TagAdapter<T, Z> getOrCreateAdapter(@NotNull PersistentDataType<T, ?> type) {
        return this.adapters.computeIfAbsent(type.getPrimitiveType(), this.CREATE_ADAPTER);
    }

    public <T, Z extends Tag> T extract(PersistentDataType<T, ?> type, Tag tag) throws ClassCastException, IllegalArgumentException {
        Class<T> primitiveType = type.getPrimitiveType();
        TagAdapter<T, Z> adapter = this.getOrCreateAdapter(type);
        Preconditions.checkArgument(adapter.isInstance(type, tag), "The found tag instance (%s) cannot store %s", tag.getClass().getSimpleName(), primitiveType.getSimpleName());
        T foundValue = adapter.extract(type, tag);
        Preconditions.checkArgument(primitiveType.isInstance(foundValue), "The found object is of the type %s. Expected type %s", foundValue.getClass().getSimpleName(), primitiveType.getSimpleName());
        return primitiveType.cast(foundValue);
    }

    private <P, T extends List<P>> ListTag constructList(@NotNull PersistentDataType<T, ?> type, @NotNull List<P> list) {
        Preconditions.checkArgument((boolean)(type instanceof ListPersistentDataType), (String)"The passed list cannot be written to the PDC with a %s (expected a list data type)", (Object)type.getClass().getSimpleName());
        ListPersistentDataType listPersistentDataType = (ListPersistentDataType)type;
        ArrayList values = Lists.newArrayListWithCapacity((int)list.size());
        for (P primitiveValue : list) {
            values.add(this.wrap(listPersistentDataType.elementType(), primitiveValue));
        }
        return new ListTag(values);
    }

    private <P> List<P> extractList(@NotNull PersistentDataType<P, ?> type, @NotNull ListTag listTag) {
        Preconditions.checkArgument((boolean)(type instanceof ListPersistentDataType), (String)"The found list tag cannot be read with a %s (expected a list data type)", (Object)type.getClass().getSimpleName());
        ListPersistentDataType listPersistentDataType = (ListPersistentDataType)type;
        ObjectArrayList output = new ObjectArrayList(listTag.size());
        for (Tag tag : listTag) {
            output.add(this.extract(listPersistentDataType.elementType(), tag));
        }
        return output;
    }

    private boolean matchesListTag(PersistentDataType<List, ?> type, Tag tag) {
        if (!(type instanceof ListPersistentDataType)) {
            return false;
        }
        ListPersistentDataType listPersistentDataType = (ListPersistentDataType)type;
        if (!(tag instanceof ListTag)) {
            return false;
        }
        ListTag listTag = (ListTag)tag;
        byte elementType = listTag.identifyRawElementType();
        TagAdapter elementAdapter = this.getOrCreateAdapter(listPersistentDataType.elementType());
        return elementAdapter.nmsTypeByte() == elementType || elementType == 0;
    }

    private record TagAdapter<P, T extends Tag>(Class<P> primitiveType, Class<T> tagType, byte nmsTypeByte, BiFunction<PersistentDataType<P, ?>, P, T> builder, BiFunction<PersistentDataType<P, ?>, T, P> extractor, BiPredicate<PersistentDataType<P, ?>, Tag> matcher) {
        private P extract(PersistentDataType<P, ?> dataType, Tag base) {
            Preconditions.checkArgument(this.tagType.isInstance(base), "The provided Tag was of the type %s. Expected type %s", base.getClass().getSimpleName(), this.tagType.getSimpleName());
            return this.extractor.apply(dataType, this.tagType.cast(base));
        }

        private T build(PersistentDataType<P, ?> dataType, Object value) {
            Preconditions.checkArgument((boolean)this.primitiveType.isInstance(value), (String)"The provided value was of the type %s. Expected type %s", value.getClass().getSimpleName(), this.primitiveType.getSimpleName());
            return (T)((Tag)this.builder.apply(dataType, this.primitiveType.cast(value)));
        }

        private boolean isInstance(PersistentDataType<P, ?> persistentDataType, Tag base) {
            return this.matcher.test(persistentDataType, base);
        }
    }
}

