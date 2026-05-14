package org.bukkit.craftbukkit.entity;

import net.minecraft.world.entity.monster.illager.AbstractIllager;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Illager;

public class CraftIllager extends CraftRaider implements Illager {

    public CraftIllager(CraftServer server, AbstractIllager entity) {
        super(server, entity);
    }

    @Override
    public AbstractIllager getHandle() {
        return (AbstractIllager) super.getHandle();
    }

    @Override
    public String toString() {
        return "Illager";
    }

	@Override
	public boolean isCelebrating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCelebrating(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}