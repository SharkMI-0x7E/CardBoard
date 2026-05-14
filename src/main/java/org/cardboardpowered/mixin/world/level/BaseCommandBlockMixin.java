package org.cardboardpowered.mixin.world.level;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.BaseCommandBlock;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.bridge.commands.CommandSourceBridge;

@Mixin(BaseCommandBlock.class)
public abstract class BaseCommandBlockMixin implements CommandSourceBridge {

    @Override
    public abstract CommandSender getBukkitSender(CommandSourceStack wrapper);

}