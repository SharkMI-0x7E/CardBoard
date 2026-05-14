package org.cardboardpowered.bridge.world.inventory;

public interface AnvilMenuBridge {

    String getNewItemName_BF();

    int getLevelCost_BF();

    void setLevelCost_BF(int i);

    int getMaxRepairCost_BF();

    void setMaxRepairCost_BF(int levels);

}