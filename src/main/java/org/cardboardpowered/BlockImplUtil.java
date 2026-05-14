package org.cardboardpowered;

import org.bukkit.craftbukkit.block.data.IMagicNumbers;
import net.minecraft.core.Direction;
import org.bukkit.block.BlockFace;

public class BlockImplUtil {

    public static BlockFace notchToBlockFace(Direction notch) {
        if (notch == null) return BlockFace.SELF;
        return BlockFace.valueOf(notch.name());
    }

    public static Direction blockFaceToNotch(BlockFace face) {
        return Direction.valueOf(face.name());
    }
    
    public static IMagicNumbers MN = null;
    public static void setMN(IMagicNumbers mn) {
        MN = mn;
    }


}