package io.papermc.paper.adventure.providers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

public class NBTLegacyHoverEventSerializer
implements LegacyHoverEventSerializer {
    public static final NBTLegacyHoverEventSerializer INSTANCE = new NBTLegacyHoverEventSerializer();
    private static final Codec<CompoundTag, String, CommandSyntaxException, RuntimeException> SNBT_CODEC = Codec.codec(TagParser::parseCompoundFully, Tag::toString);
    static final String ITEM_TYPE = "id";
    static final String ITEM_COUNT = "Count";
    static final String ITEM_TAG = "tag";
    static final String ENTITY_NAME = "name";
    static final String ENTITY_TYPE = "type";
    static final String ENTITY_ID = "id";

    protected NBTLegacyHoverEventSerializer() {
    }

    public HoverEvent.ShowItem deserializeShowItem(Component input) throws IOException {
        String raw = PlainTextComponentSerializer.plainText().serialize(input);
        try {
            CompoundTag contents = (CompoundTag)SNBT_CODEC.decode(raw);
            CompoundTag tag = contents.getCompoundOrEmpty(ITEM_TAG);
            String keyString = contents.getStringOr("id", "");
            return HoverEvent.ShowItem.showItem(Key.key(keyString), (int)contents.getByteOr(ITEM_COUNT, (byte)1), tag.isEmpty() ? null : BinaryTagHolder.encode(tag, SNBT_CODEC));
        }
        catch (CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public HoverEvent.ShowEntity deserializeShowEntity(Component input, Codec.Decoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
        String raw = PlainTextComponentSerializer.plainText().serialize(input);
        try {
            CompoundTag contents = (CompoundTag)SNBT_CODEC.decode(raw);
            String keyString = contents.getStringOr(ENTITY_TYPE, "");
            return HoverEvent.ShowEntity.showEntity((Key)Key.key((String)keyString), (UUID)UUID.fromString(contents.getStringOr("id", "")), (Component)((Component)componentCodec.decode(contents.getStringOr(ENTITY_NAME, ""))));
        }
        catch (CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public Component serializeShowItem(HoverEvent.ShowItem input) throws IOException {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", input.item().asString());
        tag.putByte(ITEM_COUNT, (byte)input.count());
        if (input.nbt() != null) {
            try {
                tag.put(ITEM_TAG, (Tag)input.nbt().get(SNBT_CODEC));
            }
            catch (CommandSyntaxException ex) {
                throw new IOException(ex);
            }
        }
        return Component.text((String)((String)SNBT_CODEC.encode(tag)));
    }

    public Component serializeShowEntity(HoverEvent.ShowEntity input, Codec.Encoder<Component, String, ? extends RuntimeException> componentCodec) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", input.id().toString());
        tag.putString(ENTITY_TYPE, input.type().asString());
        if (input.name() != null) {
            tag.putString(ENTITY_NAME, (String)componentCodec.encode(input.name()));
        }
        return Component.text((String)((String)SNBT_CODEC.encode(tag)));
    }
}

