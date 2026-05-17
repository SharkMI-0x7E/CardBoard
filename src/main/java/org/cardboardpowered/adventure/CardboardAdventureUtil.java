/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
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