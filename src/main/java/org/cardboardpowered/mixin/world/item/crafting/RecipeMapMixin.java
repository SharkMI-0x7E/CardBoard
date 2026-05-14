package org.cardboardpowered.mixin.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.*;
import org.cardboardpowered.bridge.world.item.crafting.RecipeMapBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = RecipeMap.class, priority = 1001)
public abstract class RecipeMapMixin implements RecipeMapBridge {
    @Shadow
    @Final
    public Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    @Final
    public Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

    @Shadow
    public abstract <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> recipeType);

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void createCraftBukkit(Iterable<RecipeHolder<?>> recipes, CallbackInfoReturnable<RecipeMap> cir) {
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> builder = ImmutableMultimap.builder();
        com.google.common.collect.ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> builder1 = ImmutableMap.builder();

        for (RecipeHolder<?> recipeHolder : recipes) {
            builder.put(recipeHolder.value().getType(), recipeHolder);
            builder1.put(recipeHolder.id(), recipeHolder);
        }

        // CraftBukkit start - mutable
        RecipeMap recipeMap = new RecipeMap(com.google.common.collect.LinkedHashMultimap.create(builder.build()), com.google.common.collect.Maps.newLinkedHashMap(builder1.build()));
        
        // 初始化 Fabric API 添加的 bySyncedSerializer 字段，防止 NPE
        // 并按序列化器填充配方数据，确保配方同步到客户端
        try {
            java.lang.reflect.Field bySyncedSerializerField = RecipeMap.class.getDeclaredField("bySyncedSerializer");
            bySyncedSerializerField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Map<RecipeSerializer<?>, List<RecipeHolder<?>>> bySyncedSerializerMap = 
                (Map<RecipeSerializer<?>, List<RecipeHolder<?>>>) bySyncedSerializerField.get(recipeMap);
            
            // 如果字段为 null，创建新 Map 并赋值
            if (bySyncedSerializerMap == null) {
                bySyncedSerializerMap = new IdentityHashMap<>();
                bySyncedSerializerField.set(recipeMap, bySyncedSerializerMap);
            }
            
            // 遍历 recipes，按序列化器分组填充 Map
            for (RecipeHolder<?> recipeHolder : recipes) {
                RecipeSerializer<?> serializer = recipeHolder.value().getSerializer();
                bySyncedSerializerMap.computeIfAbsent(serializer, k -> new ArrayList<>()).add(recipeHolder);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Fabric API 未安装或字段名不同，安全忽略
        }
        
        cir.setReturnValue(recipeMap);
    }

    @Override
    public void cardboard$addRecipe(RecipeHolder<?> holder) {
        Collection<RecipeHolder<?>> recipes = this.byType.get(holder.value().getType());

        if (this.byKey.containsKey(holder.id())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + holder.id());
        } else {
            recipes.add(holder);
            this.byKey.put(holder.id(), holder);
        }
    }
    // CraftBukkit end

    // Paper start - replace removeRecipe implementation
    @Override
    public <T extends RecipeInput> boolean cardboard$removeRecipe(ResourceKey<Recipe<T>> mcKey) {
        //noinspection unchecked
        final RecipeHolder<Recipe<T>> remove = (RecipeHolder<Recipe<T>>) this.byKey.remove(mcKey);
        if (remove == null) {
            return false;
        }
        final Collection<? extends RecipeHolder<? extends Recipe<T>>> recipes = this.byType(remove.value().getType());
        return recipes.remove(remove);
        // Paper end - why are you using a loop???
    }
    // Paper end - replace removeRecipe implementation
}
