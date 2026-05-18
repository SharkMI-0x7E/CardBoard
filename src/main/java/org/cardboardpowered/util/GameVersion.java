/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
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
package org.cardboardpowered.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import net.minecraft.DetectedVersion;
import net.minecraft.util.GsonHelper;
import com.google.gson.JsonObject;

public class GameVersion {

    /*
     */
    public static GameVersion create() {
        if (null != INSTANCE) return INSTANCE;
        try (InputStream inputStream = DetectedVersion.class.getResourceAsStream("/version.json");){
            if (inputStream == null) return null;
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);){
                return new GameVersion(GsonHelper.parse(inputStreamReader));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Bad version info", exception);
        }
    }

    public static GameVersion INSTANCE;

    private final int protocolVersion;
    private final String releaseTarget;
    public final int world_version;

    public GameVersion(JsonObject jsonObject) {
        INSTANCE = this;
        String relTarget = "";
        try {
			relTarget = GsonHelper.getAsString(jsonObject, "release_target");
		} catch (Exception e) {
			// Why would mojang decide to break gameversion again?
			relTarget = GsonHelper.getAsString(jsonObject, "id");
		}
		this.releaseTarget = relTarget;
        this.protocolVersion = GsonHelper.getAsInt(jsonObject, "protocol_version");
        this.world_version = GsonHelper.getAsInt(jsonObject, "world_version");
    }

    /**
     */
    public String getReleaseTarget() {
        return releaseTarget;
    }

    /**
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

}
