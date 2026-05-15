package org.cardboardpowered.conflict.model;

import java.util.ArrayList;
import java.util.List;

public class MixinConfigData {

    public String packageName;
    public List<String> mixins = new ArrayList<>();
    public List<String> server = new ArrayList<>();
    public List<String> client = new ArrayList<>();
    public String refmap;
    public String sourceModId;
    public String configFileName;
    public String configFilePath;
    public boolean required;
    public String minVersion;

    public String getFullClassName(String mixinName) {
        if (packageName == null || packageName.isEmpty()) {
            return mixinName;
        }
        return packageName + "." + mixinName;
    }

    public List<String> getAllMixins() {
        List<String> all = new ArrayList<>();
        if (mixins != null) all.addAll(mixins);
        if (server != null) all.addAll(server);
        if (client != null) all.addAll(client);
        return all;
    }

    public int getMixinCount() {
        int count = 0;
        if (mixins != null) count += mixins.size();
        if (server != null) count += server.size();
        if (client != null) count += client.size();
        return count;
    }

    public boolean isServerOnly() {
        return (mixins == null || mixins.isEmpty()) &&
               (client == null || client.isEmpty()) &&
               (server != null && !server.isEmpty());
    }

    @Override
    public String toString() {
        return "MixinConfigData{" +
                "packageName='" + packageName + '\'' +
                ", mixinCount=" + getMixinCount() +
                ", sourceModId='" + sourceModId + '\'' +
                ", configFileName='" + configFileName + '\'' +
                '}';
    }
}
