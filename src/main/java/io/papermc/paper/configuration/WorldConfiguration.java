package io.papermc.paper.configuration;

public class WorldConfiguration {
    public Lootables lootables;

    public class Lootables{
        public boolean autoReplenish = false;
        public int maxRefills = -1;
    }
}
