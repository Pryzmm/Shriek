package com.pryzmm.api;

import com.pryzmm.ShriekClient;
import com.pryzmm.api.events.ClientTalkEvent;
import com.pryzmm.api.events.ServerPlayerTalkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ShriekApi {
    private static List<Consumer<ServerPlayerTalkEvent>> serverPlayerTalkEventListeners = new ArrayList<>();
    private static List<Consumer<ClientTalkEvent>> clientTalkEventListeners = new ArrayList<>();

    /**
     * Register a consumer for a ServerPlayerTalkEvent. Whenever a player speaks,
     * it will be sent to the server and this event will be fired.
     * @param consumer The consumer to be registered
     */
    public static void registerServerPlayerSpeechListener(Consumer<ServerPlayerTalkEvent> consumer) {
        serverPlayerTalkEventListeners.add(consumer);
    }
    /**
     * Register a consumer for a ClientTalkEvent. Whenever the user speaks,
     * the event will be fired.
     * @param consumer The consumer to be registered
     */
    public static void registerClientSpeechListener(Consumer<ClientTalkEvent> consumer) {
        clientTalkEventListeners.add(consumer);
    }

    /**
     * Sets if voice messages are printed into the chat
     * @param printToChat Whether it should print to chat or not
     * @deprecated This will (probably) not print to chat. Please use your own methods to print to chat through registerServerPlayerSpeechListener()
     */
    public static void setPrintToChat(boolean printToChat) {
        ShriekClient.printToChat = printToChat;
    }
    /**
     * Sets if voice messages are printed into the console
     * @param printToConsole Whether it should print to console or not
     */
    public static void setPrintToConsole(boolean printToConsole) {
        ShriekClient.printToConsole = printToConsole;
    }

    public static void fireServerPlayerTalkEvent(ServerPlayerTalkEvent event) {
        for (Consumer<ServerPlayerTalkEvent> consumer: serverPlayerTalkEventListeners) {
            consumer.accept(event);
        }
    }

    public static void fireClientTalkEvent(ClientTalkEvent event) {
        for (Consumer<ClientTalkEvent> consumer: clientTalkEventListeners) {
            consumer.accept(event);
        }
    }

}
