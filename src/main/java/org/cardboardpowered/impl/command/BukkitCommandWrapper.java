package org.cardboardpowered.impl.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.bridge.commands.CommandSourceStackBridge;

import org.cardboardpowered.bridge.commands.CommandSourceBridge;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

public class BukkitCommandWrapper implements com.mojang.brigadier.Command<CommandSourceStack>, Predicate<CommandSourceStack>, SuggestionProvider<CommandSourceStack> {

    private final Command command;

    public BukkitCommandWrapper(Command command) {
        this.command = command;
    }

    public LiteralCommandNode<CommandSourceStack> register(CommandDispatcher<CommandSourceStack> dispatcher, String label) {
        return dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal(label).requires(this).executes(this)
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("args", StringArgumentType.greedyString()).suggests(this).executes(this))
        );
    }

    @Override
    public boolean test(CommandSourceStack wrapper) {
        return true; // Let Bukkit handle permissions
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            return Bukkit.getServer().dispatchCommand(getSender(context.getSource()),context.getInput()) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public CommandSender getSender(CommandSourceStack source) {
        try {
            ServerPlayer plr = source.getPlayer();
            if (null != plr)
                return ((CommandSourceBridge)plr).getBukkitSender(source);
        } catch (Exception ignored) {
            //ex.printStackTrace();
        }
        Entity e = source.getEntity();
        return (null != e) ? ((CommandSourceBridge)e).getBukkitSender(source) : null;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> results = ((CraftServer)Bukkit.getServer()).tabComplete(((CommandSourceStackBridge) context.getSource()).getBukkitSender(), builder.getInput(), context.getSource().getLevel(), context.getSource().getPosition(), true);

        // Defaults to sub nodes, but we have just one giant args node, so offset accordingly
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

        for (String s : results) builder.suggest(s);
        return builder.buildFuture();
    }

}