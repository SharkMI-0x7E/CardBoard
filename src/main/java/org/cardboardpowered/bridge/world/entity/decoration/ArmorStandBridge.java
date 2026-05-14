package org.cardboardpowered.bridge.world.entity.decoration;

public interface ArmorStandBridge {

    void setHideBasePlateBF(boolean b);

    void setShowArmsBF(boolean arms);

    void setSmallBF(boolean small);

    void setMarkerBF(boolean marker);

    boolean canMoveBF();

    void setCanMoveBF(boolean b);

}