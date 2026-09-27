package com.misterd.agritechevolved.network;

import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.datamap.SoilModifierData;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Pushes the server's data map contents to a client. NeoForge syncs {@code DataMapType}
 * registries automatically; Forge 47.x has no such facility, so a client would otherwise
 * only ever see the values present in its own resource packs and would miss anything a
 * server-side datapack or mod contributes.
 */
public record ATESyncDataMapsPacket(Map<Item, FertilizerData> fertilizers,
                                     Map<Item, SoilModifierData> soilModifiers) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void encode(ATESyncDataMapsPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.fertilizers.size());
        for (Map.Entry<Item, FertilizerData> entry : packet.fertilizers.entrySet()) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.getKey()));
            buf.writeFloat(entry.getValue().speedMultiplier());
            buf.writeFloat(entry.getValue().yieldMultiplier());
        }

        buf.writeVarInt(packet.soilModifiers.size());
        for (Map.Entry<Item, SoilModifierData> entry : packet.soilModifiers.entrySet()) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(entry.getKey()));
            buf.writeFloat(entry.getValue().growthModifier());
        }
    }

    public static ATESyncDataMapsPacket decode(FriendlyByteBuf buf) {
        Map<Item, FertilizerData> fertilizers = new HashMap<>();
        int fertilizerCount = buf.readVarInt();
        for (int i = 0; i < fertilizerCount; i++) {
            Item item = itemOf(buf.readResourceLocation());
            fertilizers.put(item, new FertilizerData(buf.readFloat(), buf.readFloat()));
        }

        Map<Item, SoilModifierData> soilModifiers = new HashMap<>();
        int soilCount = buf.readVarInt();
        for (int i = 0; i < soilCount; i++) {
            Item item = itemOf(buf.readResourceLocation());
            soilModifiers.put(item, new SoilModifierData(buf.readFloat()));
        }

        return new ATESyncDataMapsPacket(fertilizers, soilModifiers);
    }

    public static void handle(ATESyncDataMapsPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.setPacketHandled(true);
        ATEDataMaps.acceptSync(packet.fertilizers(), packet.soilModifiers());
        LOGGER.debug("[ATE] Synced data maps from server: {} fertilizers, {} soil modifiers",
                packet.fertilizers.size(), packet.soilModifiers.size());
    }

    public static void sendTo(ServerPlayer player) {
        ATENetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), snapshot());
    }

    public static ATESyncDataMapsPacket snapshot() {
        return new ATESyncDataMapsPacket(
                Map.copyOf(ATEDataMaps.serverFertilizers()),
                Map.copyOf(ATEDataMaps.serverSoilModifiers()));
    }

    private static Item itemOf(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
    }
}
