package org.cardboardpowered.mixin.server.level;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.ContainerBridge;
import com.mojang.authlib.GameProfile;

// TODO: Fix this whole thing as some items don't drop.
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin_DeathEvent extends Player {
	public ServerPlayerMixin_DeathEvent(Level w, GameProfile gp) {
		super(w, gp);
	}

	// Bukkit start
	public boolean keepLevel = false;
	// Bukkit end
	
	private AtomicReference<String> cardboard$deathString = new AtomicReference<>("null");
    private AtomicReference<String> cardboard$deathMsg = new AtomicReference<>("null");

    private AtomicReference<PlayerDeathEvent> cardboard$deathEvent = new AtomicReference<>();

    private ServerPlayer cb$this() {
    	return (ServerPlayer)(Object)this;
    }

    @Inject(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;",
            ordinal = 0),
            cancellable = true)
    private void cardboard$do_PlayerDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        // CraftBukkit start - fire PlayerDeathEvent
        if (cb$this().isRemoved()) {
            ci.cancel();
        }

        java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<>(cb$this().getInventory().getContainerSize());

        Boolean keepInventory = cb$this().level().getGameRules().get(GameRules.KEEP_INVENTORY) || cb$this().isSpectator();
        // boolean keepInventory = cb$this().getEntityWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || cb$this().isSpectator();

        if (!keepInventory) {
            for (ItemStack item : ((ContainerBridge) ((ServerPlayer) (Object) this).getInventory()).getContents()) {
                if (!item.isEmpty() && !EnchantmentHelper.has(item, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                    loot.add(CraftItemStack.asCraftMirror(item));
                }
            }
        }
        // SPIGOT-5071: manually add player loot tables (SPIGOT-5195 - ignores keepInventory rule)
        this.dropFromLootTable(cb$this().level(), damageSource, cb$this().lastHurtByPlayerMemoryTime > 0);

        ((EntityBridge)(Object)this).cardboard_getDrops();

        for (org.bukkit.inventory.ItemStack item : ((EntityBridge)(Object)this).cardboard_getDrops()) {
            loot.add(item);
        }
        // SPIGOT-5188: make sure to clear
        ((EntityBridge)(Object)this).cardboard_getDrops().clear();

        Component defaultMessage = cb$this().getCombatTracker().getDeathMessage();
        String deathmessage = defaultMessage.getString();
        cardboard$deathMsg.set(deathmessage);
        keepLevel = keepInventory; // SPIGOT-2222: pre-set keepLevel
        org.bukkit.event.entity.PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(((ServerPlayer) (Object) this), damageSource, loot, deathmessage, keepInventory);
        cardboard$deathEvent.set(event);

        // SPIGOT-943 - only call if they have an inventory open
        if (cb$this().containerMenu != cb$this().inventoryMenu) {
            this.closeContainer();
        }

        String deathMessage = event.getDeathMessage();
        cardboard$deathString.set(deathMessage);
    }

    @Inject(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"),
            cancellable = true)
    private void cardboard$check_if_dead(DamageSource damageSource, CallbackInfo ci) {
        boolean cardboard$flag = cardboard$deathString.get() != null && !cardboard$deathString.get().isEmpty();
        if (!cardboard$flag) { // TODO: allow plugins to override?
            ci.cancel();
        }
    }

    @Redirect(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"))
    private Component cardboard$redirect_death_message(CombatTracker instance) {
        Component cardboard$component;
        if (cardboard$deathString.get().equals(cardboard$deathMsg.get())) {
            cardboard$component = instance.getDeathMessage();
        } else {
            cardboard$component = CraftChatMessage.fromStringOrNull(cardboard$deathString.get());
        }
        return cardboard$component;
    }

    @Inject(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
    private void cardboard$check_event_drops(DamageSource damageSource, CallbackInfo ci) {
        // SPIGOT-5478 must be called manually now
    	// cb$this().dropXp(damageSource.getAttacker());
    	
    	this.dropExperience(cb$this().level(), damageSource.getEntity());
    	
        // we clean the player's inventory after the EntityDeathEvent is called so plugins can get the exact state of the inventory.
        if (!cardboard$deathEvent.get().getKeepInventory()) {
        	cb$this().getInventory().clearContent();
        }
    }

    @Redirect(method = "die",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void cardboard$cancel_vanilla_drop(ServerPlayer instance, ServerLevel world, DamageSource damageSource) {
    }
    
    // Lnet/minecraft/world/entity/LivingEntity;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V
    // Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V
    // Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V
    
    
    // TODO: 1.20.4
    /*@Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/Scoreboard;forEachScore(Lnet/minecraft/scoreboard/ScoreboardCriterion;Ljava/lang/String;Ljava/util/function/Consumer;)V "))
    private void cardboard$use_bukkit_scoreboard(Scoreboard instance, ScoreboardCriterion criteria, String scoreboardName, Consumer<ScoreboardPlayerScore> action) {
    	cb$this().setCameraEntity(((ServerPlayerEntity) (Object) this));
        CraftServer.INSTANCE.getScoreboardManager().getScoreboardScores(ScoreboardCriterion.DEATH_COUNT, cb$this().getEntityName(), ScoreboardPlayerScore::incrementScore);
    }*/
    
    

	
}
