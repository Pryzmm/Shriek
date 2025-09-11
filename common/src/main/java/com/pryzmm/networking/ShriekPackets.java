package com.pryzmm.networking;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import com.pryzmm.networking.packets.PlayerSpeakPacket;

import java.nio.channels.NetworkChannel;

public class ShriekPackets {
    private static NetworkChannel INSTANCE;

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), PlayerSpeakPacket.PLAYER_SPEAK_PACKET_TYPE,
                PlayerSpeakPacket.STREAM_CODEC, PlayerSpeakPacket::handleServerSide);
    }

    public static void sendToServer(@NotNull CustomPacketPayload message) {
        if (NetworkManager.canServerReceive(message.type())) {
            NetworkManager.sendToServer(message);
        }
    }
}