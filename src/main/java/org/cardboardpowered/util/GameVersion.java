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
