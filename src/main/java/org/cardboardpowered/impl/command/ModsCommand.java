package org.cardboardpowered.impl.command;

import com.google.common.collect.ImmutableList;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a /fabricmods command
 */
public class ModsCommand extends Command {

    public ModsCommand(String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/fabricmods";
        
        List<String> aka = Arrays.asList("mymods", "mods");
        
        this.setAliases(aka);
        this.setPermission("cardboard.command.mods");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (sender.hasPermission("cardboard.command.mods")) {
            String mods = "";
            int count = 0;
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                String name = mod.getMetadata().getName();

                if (name.startsWith("Fabric") && name.endsWith(")")) continue; // Don't list all modules of FAPI
                if (name.startsWith("Fabric API Base")) name = "Fabric API";
                if (name.startsWith("OpenJDK") || name.startsWith("SpecialSource")) continue;
                if (name.startsWith("Fabric Convention Tags") || name.startsWith("MixinExtras")) continue;
                if (name.contains("-bundle")) continue;

                if (!mods.contains(name)) {
                	mods += ", " + ChatColor.GREEN + name + ChatColor.WHITE;
                	count += 1;
                }
            }
            sender.sendMessage("Mods (" + count + "): " + mods.substring(2));
        } else {
            sender.sendMessage("No Permission for command! Missing permission: cardboard.command.mods");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return ImmutableList.of();
    }

}
