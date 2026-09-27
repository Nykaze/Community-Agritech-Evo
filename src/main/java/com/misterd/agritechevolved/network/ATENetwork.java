package com.misterd.agritechevolved.network;

import com.misterd.agritechevolved.AgritechEvolved;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ATENetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AgritechEvolved.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId;

    public static void register() {
        CHANNEL.messageBuilder(ATESyncDataMapsPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ATESyncDataMapsPacket::encode)
                .decoder(ATESyncDataMapsPacket::decode)
                .consumerMainThread(ATESyncDataMapsPacket::handle)
                .add();
    }

    private ATENetwork() {
    }
}
