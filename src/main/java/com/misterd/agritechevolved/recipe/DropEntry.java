package com.misterd.agritechevolved.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;

public record DropEntry(Item item, int min, int max, float chance) {

    public static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(DropEntry::item),
            Codec.INT.fieldOf("min").forGetter(DropEntry::min),
            Codec.INT.fieldOf("max").forGetter(DropEntry::max),
            Codec.FLOAT.fieldOf("chance").forGetter(DropEntry::chance)
    ).apply(instance, DropEntry::new));

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(BuiltInRegistries.ITEM.getId(item));
        buf.writeVarInt(min);
        buf.writeVarInt(max);
        buf.writeFloat(chance);
    }

    public static DropEntry fromNetwork(FriendlyByteBuf buf) {
        return new DropEntry(
                BuiltInRegistries.ITEM.byId(buf.readVarInt()),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat()
        );
    }
}
