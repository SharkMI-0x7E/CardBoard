package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.item.MapItem;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

@MixinInfo(events = {"MapInitializeEvent"})
@Mixin(MapItem.class)
public class MapItemMixin {

    /**
     * @reason .
     * @author .
     */// TODO 1.17ify
    /*@Overwrite
    private static MapState createMapState(ItemStack itemstack, World world, int i, int j, int k, boolean flag, boolean flag1, RegistryKey<World> resourcekey) {
        int l = world.getNextMapId();
        MapState worldmap = new MapState("map_" + l);

        worldmap.init(i, j, k, flag, flag1, resourcekey);
        world.putMapState(worldmap);
        itemstack.getOrCreateTag().putInt("map", l);

        MapInitializeEvent event = new MapInitializeEvent(((IMixinMapState)worldmap).getMapViewBF());
        Bukkit.getServer().getPluginManager().callEvent(event);
        return worldmap;
    }*/

}
