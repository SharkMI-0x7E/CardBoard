/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.conflict.model;

import java.util.ArrayList;
import java.util.List;

public class MixinConfigData {

    private String packageName;
    private List<String> mixins = new ArrayList<>();
    private List<String> server = new ArrayList<>();
    private List<String> client = new ArrayList<>();
    private String refmap;
    private String sourceModId;
    private String configFileName;
    private String configFilePath;
    private boolean required;
    private String minVersion;

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public List<String> getMixins() { return mixins; }
    public void setMixins(List<String> mixins) { this.mixins = mixins; }

    public List<String> getServer() { return server; }
    public void setServer(List<String> server) { this.server = server; }

    public List<String> getClient() { return client; }
    public void setClient(List<String> client) { this.client = client; }

    public String getRefmap() { return refmap; }
    public void setRefmap(String refmap) { this.refmap = refmap; }

    public String getSourceModId() { return sourceModId; }
    public void setSourceModId(String sourceModId) { this.sourceModId = sourceModId; }

    public String getConfigFileName() { return configFileName; }
    public void setConfigFileName(String configFileName) { this.configFileName = configFileName; }

    public String getConfigFilePath() { return configFilePath; }
    public void setConfigFilePath(String configFilePath) { this.configFilePath = configFilePath; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public String getMinVersion() { return minVersion; }
    public void setMinVersion(String minVersion) { this.minVersion = minVersion; }

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
