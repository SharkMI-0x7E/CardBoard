package org.cardboardpowered.bridge.world.entity.animal.equine;

public interface AbstractHorseBridge {
    void cardboard$setMouthOpen(boolean open);

    boolean cardboard$isMouthOpen();

    int cardboard$getMaxDomestication();

    void cardboard$setMaxDomestication(int maxDomestication);
}
