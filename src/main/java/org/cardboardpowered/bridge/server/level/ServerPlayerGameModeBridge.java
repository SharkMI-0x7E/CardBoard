package org.cardboardpowered.bridge.server.level;

public interface ServerPlayerGameModeBridge {

    boolean getInteractResultBF();
    void setInteractResultBF(boolean b);

    void setFiredInteractBF(boolean b);
    boolean getFiredInteractBF();

}