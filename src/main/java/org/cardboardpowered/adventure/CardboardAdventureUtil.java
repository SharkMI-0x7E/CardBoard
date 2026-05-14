package org.cardboardpowered.adventure;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.ComponentSerialization;

/**
 */
public final class CardboardAdventureUtil {

    private static final Gson GSON = new GsonBuilder().create();

    private CardboardAdventureUtil() {}

    public static Component toAdventure(net.minecraft.network.chat.Component text) {
        if (text == null) {
            return Component.empty();
        }
        // Serialize Text->Json
        String json = GSON.toJson(ComponentSerialization.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, text)
                .result()
                .orElseThrow(() -> new IllegalArgumentException("Unable to encode Text")));
        // Deserialize Json->Adventure Component
        return GsonComponentSerializer.gson().deserialize(json);
    }

}