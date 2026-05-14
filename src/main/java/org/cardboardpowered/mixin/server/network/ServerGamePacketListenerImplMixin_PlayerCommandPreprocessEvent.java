package org.cardboardpowered.mixin.server.network;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.server.network.ServerGamePacketListenerImplBridge;
import com.mojang.brigadier.ParseResults;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.impl.util.LazyPlayerSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.Map;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageChain.DecodeException;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.FutureChain;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 800)
public abstract class ServerGamePacketListenerImplMixin_PlayerCommandPreprocessEvent implements ServerGamePacketListenerImplBridge {

	@Shadow
	public ServerPlayer player;

	// Lnet/minecraft/server/network/ServerPlayNetworkHandler;handleCommandExecution(Lnet/minecraft/network/packet/c2s/play/ChatCommandSignedC2SPacket;Lnet/minecraft/network/message/LastSeenMessageList;)V
	
	/**
	 * @reason PlayerCommandPreprocessEvent
	 * @author Cardboard mod
	 * @since 1.21.4
	 */
	@Overwrite
	public void performUnsignedChatCommand(String command) {
        String command1 = "/" + command;
        //if (SpigotConfig.logCommands) {
        	CardboardMod.LOGGER.info(this.player.getScoreboardName() + " issued server command: " + command1);
        //}
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(this.getPlayer(), command1, new LazyPlayerSet(CraftServer.server));
        CraftServer.INSTANCE.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        command = event.getMessage().substring(1);
        ParseResults<CommandSourceStack> parseresults = this.parseCommand(command);
        
        if(CardboardConfig.REGISTRY_COMMAND_FIX) {
			Bukkit.dispatchCommand(getPlayer(), command);
			return;
		}
        
        if (CraftServer.server.enforceSecureProfile() && SignableCommand.hasSignableArguments(parseresults)) {
        	// CardboardMod.LOGGER.error("Received unsigned command packet from {}, but the command requires signable arguments: {}", (Object)this.player.getGameProfile().getName(), (Object)command);
            // this.player.sendMessage(INVALID_COMMAND_SIGNATURE_TEXT);
        } else {
        	CraftServer.server.getCommands().performCommand(parseresults, command);
        }
    }
	
	/**
	 * @reason PlayerCommandPreprocessEvent
	 * @author Cardboard mod
	 */
	@SuppressWarnings("unused")
	@Overwrite
	// private void handleCommandExecution(CommandExecutionC2SPacket packet, LastSeenMessageList lastseenmessages) {
	private void performSignedChatCommand(ServerboundChatCommandSignedPacket packet, LastSeenMessages lastSeenMessages) {
		
		CardboardMod.LOGGER.info("HANDLE COMMAND EXEC!");
		
		PlayerChatMessage playerchatmessage;
		String command = "/" + packet.command();
		CardboardMod.LOGGER.info(this.player.getGameProfile().name() + " issued server command: " + command);
		PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(this.getPlayer(), command, new LazyPlayerSet(CraftServer.server));
		CraftServer.INSTANCE.getPluginManager().callEvent(event);

		if(event.isCancelled()) {
			return;
		}

		command = event.getMessage().substring(1);

		
		if(CardboardConfig.REGISTRY_COMMAND_FIX) {
			Bukkit.dispatchCommand(getPlayer(), command);
			return;
		}

		ParseResults<CommandSourceStack> parseresults = this.parseCommand(packet.command());
		Map<String, PlayerChatMessage> map;
		try {
			map = (packet.command().equals(command)) ? this.collectSignedArguments(packet, SignableCommand.of(parseresults), lastSeenMessages) : Collections.emptyMap(); // CraftBukkit
		} catch(SignedMessageChain.DecodeException e) {
			this.handleMessageDecodeFailure(e);
			return;
		}

		CommandSigningContext.SignedArguments arguments = new CommandSigningContext.SignedArguments(map);

		parseresults = Commands.mapSource(parseresults, (stack) ->
				stack.withSigningContext(arguments, chatMessageChain));
		CraftServer.server.getCommands().performCommand(parseresults, command);
	}

	@Shadow
	public void handleMessageDecodeFailure(SignedMessageChain.DecodeException e) {
	}

	//  private Map<String, SignedMessage> collectArgumentMessages(CommandExecutionC2SPacket packet, DecoratableArgumentList<?> arguments) {
	@Shadow
	private Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandSignedPacket packet, SignableCommand<?> a, LastSeenMessages b) throws DecodeException {
		return null; // Shadow method
	}

	@Shadow
	private ParseResults<CommandSourceStack> parseCommand(String command) {
		return null; // Shadow method
	}

	@Shadow @Final private FutureChain chatMessageChain;
	
	/*
	public CraftPlayer getPlayer() {
		return (CraftPlayer) ((IMixinServerEntityPlayer) (Object) this.player).getBukkitEntity();
	}
	*/

}
