package org.cardboardpowered.mixin.world.level;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.world.level.LevelBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.storage.WritableLevelData;
import org.bukkit.craftbukkit.block.CapturedBlockState;
import org.cardboardpowered.impl.world.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelBridge {

    @Shadow public LevelChunk getChunkAt(BlockPos pos) {return null;}
    private CraftWorld bukkit;

    public boolean captureBlockStates = false;
    public boolean captureTreeGeneration = false;
    public Map<BlockPos, CapturedBlockState> capturedBlockStates = new HashMap<>();

    @Shadow
    public abstract LevelEntityGetter<Entity> getEntities();

    @Shadow
    public ResourceKey<Level> dimension() {
        return null;
    }

    @Override
    public LevelEntityGetter<Entity> cb$get_entity_lookup() {
    	return getEntities();
    }
    
    @Override
    public Map<BlockPos, CapturedBlockState> getCapturedBlockStates_BF() {
        return capturedBlockStates;
    }

    @Override
    public boolean isCaptureBlockStates_BF() {
        return captureBlockStates;
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(WritableLevelData a, ResourceKey<?> b, RegistryAccess rm, Holder<DimensionType> registryEntry, boolean f, boolean g, long h, int i, CallbackInfo ci) {

        if (!(((Level) (Object) this) instanceof ServerLevel)) {
            System.out.println("CLIENT WORLD!");
            return;
        }

        Level thiz = (Level) (Object) this;
        ServerLevel nms = ((ServerLevel) thiz);
    	CardboardMod.on_world_init_mc(nms);
    }

    @Override
    public CraftWorld cardboard$getWorld() {
        return bukkit;
    }

    @Override
    public void set_bukkit_world(CraftWorld world) {
        this.bukkit = world;
    }

    @Inject(at = @At("HEAD"), method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
    public void setBlockState1(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        // TODO 1.17ify: if (!ServerWorld.isOutOfBuildLimitVertically(blockposition)) {
            LevelChunk chunk = getChunkAt(pos);
            boolean captured = false;
            if (this.captureBlockStates && !this.capturedBlockStates.containsKey(pos)) {
                CapturedBlockState blockstate = CapturedBlockState.getTreeBlockState((Level)(Object)this, pos, flags);
                this.capturedBlockStates.put(pos.immutable(), blockstate);
                captured = true;
            }
        //}
    }

    @Override
    public void setCaptureBlockStates_BF(boolean b) {
        this.captureBlockStates = b;
    }
}
